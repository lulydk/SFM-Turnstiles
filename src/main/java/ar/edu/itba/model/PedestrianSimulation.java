package ar.edu.itba.model;

import ar.edu.itba.utils.Utils;
import static ar.edu.itba.utils.Utils.METHOD_EMPTINESS;

import java.util.*;
import java.util.stream.Collectors;

public class PedestrianSimulation {
    private final double dt;
    private final Board board;
    private final int maxIterations;
    private final List<Turnstile> turnstiles;
    private final List<Agent> agents;
    private final List<Wall> walls;
    private final SFM sfm;

    private double t;
    private int step;
    private final Map<Integer, Map<Board.Cell, List<Agent>>> boardState;

    private final int Y_HORIZON = 2;

    private final static double PADDING = 2.0;

    public PedestrianSimulation(double dimL, int cellLineCount, double dt, int maxAgents, double mass, double minD, double maxD, double desiredSpeed, int maxIterations, String method, SFM sfm, int turnstileCount, double turnstileWidth, double corridorLength) {
        t = 0;
        this.dt = dt;
        this.sfm = sfm;
        this.maxIterations = maxIterations;

        board = new Board(dimL, cellLineCount);
        walls = setWalls(dimL, corridorLength);
        turnstiles = setTurnstiles(turnstileCount, turnstileWidth, corridorLength, dimL);

        boardState = new HashMap<>();
        step = 0;
        agents = setAgents(maxAgents, mass, minD, maxD, desiredSpeed, method);
        t += dt;
        step++;
    }

    public Board getBoard() {
        return board;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public List<Turnstile> getTurnstiles() {
        return turnstiles;
    }

    public Map<Integer, Map<Board.Cell, List<Agent>>> getBoardState() {
        return boardState;
    }

    public double getDt() {
        return dt;
    }

    public int getStep() {
        return step;
    }

    public void run() {
        while (step < maxIterations && !boardState.get(step-1).isEmpty()) {
            if (step % 100 == 0) {
                System.out.println("Iteration: " + step +"; time: " + t);
            }
            Map<Board.Cell, List<Agent>> lastState = boardState.get(step-1);
            boardState.put(step, new HashMap<>());
//            for (Turnstile turnstile : turnstiles) {
//                if (turnstile.isLocked() && turnstile.lockTimeFinished(t)) {
//                    turnstile.setLocked(false);
//                    turnstile.removeFromQueue(1);
//                }
//            }
            Map<Agent, Vector> forces = lastState
                    .entrySet()
                    .stream()
                    .flatMap(e -> {
                        Board.Cell cell = e.getKey();
                        List<Agent> allAgents = new ArrayList<>(lastState.get(cell));
                        for (Board.Cell neighborCell : cell.getNeighbors()) {
                            if (lastState.containsKey(neighborCell)) {
                                allAgents.addAll(lastState.get(neighborCell));
                            }
                        }
                        return e.getValue().stream().map(agent -> {
                            /*
                            if (agent.getAdvancement() == Agent.Advancement.ON_TRANSACTION) {
                                agent.setCurrentState(new Agent.State(agent.getCurrentPosition(), agent.getCurrentVelocity()));
                                return Map.entry(agent, Vector.ZERO);
                            }
                             */

                            List<Agent> neighbors = new ArrayList<>(allAgents);
                            neighbors.remove(agent);
                            Vector force = sfm.computeForce(agent, neighbors, walls);
                            return Map.entry(agent, force);
                        });
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            forces.forEach((agent, force) -> {
                Verlet.step(agent, force, dt);

                Turnstile turnstile = agent.getTargetTurnstile();
                if (agent.getAdvancement() == Agent.Advancement.DEFAULT) {
                    agent.setTargetPoint(turnstile.getCorridorCenterPosition().add(new Point(0,0.8)));
                }
                if (turnstile.isAligned(agent.getCurrentPosition(), agent.getRadius())) {
                    agent.setTargetPoint(turnstile.getCenterPosition().add(new Point(0,0.8)));
                    if (agent.getAdvancement() == Agent.Advancement.DEFAULT && agent.getCurrentPosition().y() > turnstile.getCorridorCenterPosition().y()) {
                        agent.setAdvancement(Agent.Advancement.ON_CORRIDOR);
                    } else if (agent.getAdvancement() == Agent.Advancement.ON_CORRIDOR) {
                        if (!turnstile.isLocked() && turnstile.minimumDistance(agent.getCurrentPosition()).getMagnitude() <= agent.getRadius()) {
                            agent.setAdvancement(Agent.Advancement.ON_TURNSTILE);
                            turnstile.setLocked(true);
                        }
                    } else if (agent.getAdvancement() == Agent.Advancement.ON_TURNSTILE) {
                        Point newTargetPoint = turnstile.getCenterPosition().add(new Point(0,0.8));
                        agent.setTargetPoint(newTargetPoint);
                        if (newTargetPoint.distance(agent.getCurrentPosition()) <= agent.getRadius()) {
                            agent.setAdvancement(Agent.Advancement.ON_TRANSACTION);
                            turnstile.setBlockTime(t);
                        }
                    } else if (agent.getAdvancement() == Agent.Advancement.ON_TRANSACTION && turnstile.lockTimeFinished(t)) {
                        agent.setAdvancementFinished(t);
                        turnstile.setLocked(false);
                    } else if (agent.getAdvancement() == Agent.Advancement.FINISHED_TRANSACTION) {
                        agent.setTargetPoint(turnstile.getCenterPosition().add(new Point(0,Y_HORIZON)));
                    }
                }

                if (!agent.hasPassed(board.getDimL() + Y_HORIZON)) {
                    for (Board.Cell neighborCell : getBoard().getCells()) {
                        if (neighborCell.isInCell(agent.getCurrentPosition())) {
                            boardState.get(step).putIfAbsent(neighborCell, new ArrayList<>());
                            boardState.get(step).get(neighborCell).add(agent);
                            break;
                        }
                    }
                }
                else {
                    System.out.println("Agent: "+agent.getId()+" escaped");
                }
            });
            t += dt;
            step++;
        }
    }

    private List<Wall> setWalls(double dimL, double corridorLength) {
        System.out.println("Setting board...");
        List<Wall> newWalls = new ArrayList<>();
        newWalls.add(new Wall(new Point(PADDING, dimL-PADDING-corridorLength), new Point(PADDING, PADDING)));

        //newWalls.add(new Wall(new Point(PADDING, dimL-PADDING), new Point(dimL-PADDING, dimL-PADDING)));

        newWalls.add(new Wall(new Point(dimL-PADDING, PADDING), new Point(dimL-PADDING, dimL-PADDING-corridorLength)));
        newWalls.add(new Wall(new Point(PADDING, PADDING), new Point(dimL-PADDING, PADDING)));
        return newWalls;
    }

    private List<Turnstile> setTurnstiles(int maxCount, double width, double corridorLength, double dimL) {
        List<Turnstile> newTurnstiles = new ArrayList<>();
        double spaceBetweenTurnstiles = (dimL - PADDING*2 - width*maxCount) / (maxCount+1);
        Point lastPoint = new Point(PADDING, dimL-PADDING-corridorLength);
        for (int i = 0; i < maxCount; i++) {
            Wall newWall = new Wall(new Point(lastPoint.x() + spaceBetweenTurnstiles, lastPoint.y()), lastPoint);
            walls.add(newWall);
            Point middlePoint = new Point(lastPoint.x() + spaceBetweenTurnstiles + width/2, dimL-PADDING);
            Turnstile newTurnstile = new Turnstile(i, middlePoint, width, corridorLength);
            newTurnstiles.add(newTurnstile);
            lastPoint = new Point(middlePoint.x() + width/2, lastPoint.y());
            walls.add(
                    new Wall(newTurnstile.getCorridor().getStartPoint(), newTurnstile.getStartPoint())
            );
            walls.add(
                    new Wall(newTurnstile.getStartPoint(), newTurnstile.getCorridor().getStartPoint())
            );
            walls.add(
                    new Wall(newTurnstile.getCorridor().getEndPoint(), newTurnstile.getEndPoint())
            );
            walls.add(
                    new Wall(newTurnstile.getEndPoint(), newTurnstile.getCorridor().getEndPoint())
            );
        }
        walls.add(new Wall(new Point(lastPoint.x() + spaceBetweenTurnstiles, lastPoint.y()), lastPoint));
        return newTurnstiles;
    }

    private List<Agent> setAgents(int maxAgents, double mass, double minD, double maxD, double desiredSpeed, String method) {
        System.out.println("Setting agents...");
        boardState.put(step, new HashMap<>());
        List<Agent> newAgents = new ArrayList<>();
        for (int i = 0; i < maxAgents; i++) {
            Point initialPosition = Utils.getRandomPoint(i, PADDING+maxD, PADDING+maxD, board.getDimL()-PADDING-maxD, board.getDimL() * 0.45, newAgents, maxD);
            Turnstile assignedTurnstile = assignTurnstile(method, initialPosition);
            Agent newAgent = new Agent(
                    i,
                    mass,
                    Utils.getUniformRadius(i,minD,maxD),
                    initialPosition,
                    new Vector(
                            desiredSpeed,
                            initialPosition.getVector(assignedTurnstile.getCorridorCenterPosition()).getAngle()
                    ),
                    assignedTurnstile
            );
            newAgents.add(newAgent);
            for (Board.Cell cell : board.getCells()) {
                if (cell.isInCell(initialPosition)) {
                    boardState.get(step).putIfAbsent(cell, new ArrayList<>());
                    boardState.get(step).get(cell).add(newAgent);
                    break;
                }
            }
        }
        return newAgents;
    }

    private Turnstile assignTurnstile(String method, Point agentPosition) {
        Turnstile turnstile;
        if (Objects.equals(method, METHOD_EMPTINESS)) {
            turnstile = turnstiles.stream()
                    .min(Comparator.comparing(Turnstile::getQueue))
                    .orElseThrow(() -> new IllegalStateException("No turnstiles available"));
        } else { // METHOD_CLOSENESS
            turnstile = turnstiles.stream()
                    .min(Comparator.comparing(t -> t.getCorridorCenterPosition().distance(agentPosition)))
                    .orElseThrow(() -> new IllegalStateException("No turnstiles available"));
        }
        turnstile.addToQueue(1);
        return turnstile;
    }

    public double getPadding() {
        return PADDING;
    }
}
