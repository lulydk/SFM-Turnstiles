package ar.edu.itba.model;

import ar.edu.itba.utils.Utils;
import static ar.edu.itba.utils.Utils.METHOD_EMPTINESS;

import java.util.*;
import java.util.function.Function;
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
    private final Map<Double, Map<Board.Cell, List<Agent>>> boardState;

    private final int Y_HORIZON = 2;

    private final static double PADDING = 0.8;

    public PedestrianSimulation(double dimL, int cellLineCount, double dt, int maxAgents, double mass, double minD, double maxD, double desiredSpeed, int maxIterations, String method, SFM sfm, int turnstileCount, double turnstileWidth, double corridorLength) {
        t = 0;
        this.dt = dt;
        this.sfm = sfm;
        this.maxIterations = maxIterations;

        board = new Board(dimL, cellLineCount);
        walls = setWalls(dimL, corridorLength);
        turnstiles = setTurnstiles(turnstileCount, turnstileWidth, corridorLength, dimL);

        boardState = new HashMap<>();
        agents = setAgents(maxAgents, mass, minD, maxD, desiredSpeed, method);
        t += dt;
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

    public Map<Double, Map<Board.Cell, List<Agent>>> getBoardState() {
        return boardState;
    }

    public void run() {
        while (boardState.size() < maxIterations && !boardState.get(t-dt).isEmpty()) {
            System.out.println("Iteration: " + boardState.size());
            Map<Board.Cell, List<Agent>> lastState = boardState.get(t-dt);
            boardState.put(t, new HashMap<>());
            /*
            for (Turnstile turnstile : turnstiles) {
                if (turnstile.isLocked() && turnstile.lockTimeFinished(t)) {
                    System.out.println("//// unlocking turnstile "+turnstile.getId());
                    turnstile.setLocked(false);
                    turnstile.removeFromQueue(1);
                }
            }
            */
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
                            if (agent.getTargetTurnstile().minimumDistance(agent.getCurrentPosition()).getMagnitude() <= agent.getRadius()) {
                            // if (agent.isInTurnstile()) {
                                agent.setInTurnstile(agent.getTargetTurnstile().isLocked());
                                agent.setCurrentState(new Agent.State(agent.getCurrentPosition(), agent.getCurrentVelocity()));
                                return Vector.ZERO;
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
                if (turnstile.isAligned(agent.getCurrentPosition(), agent.getRadius())) {
                    agent.setTargetPoint(turnstile.getCenterPosition().add(new Point(0,Y_HORIZON)));
                    /*
                    if (!turnstile.isLocked() && (turnstile.isPointInSegment(agent.getCurrentPosition()) || turnstile.minimumDistance(agent.getCurrentPosition()).getMagnitude() <= agent.getRadius())) {
                        //System.out.println("+ Blocking turnstile "+turnstile.getId());
                        turnstile.setBlockTime(t);
                        agent.setInTurnstile(true);
                    }
                     */
                }
                else
                    agent.setTargetPoint(turnstile.getCorridorCenterPosition().add(new Point(0,Y_HORIZON)));
//                        if (Math.abs(agent.getCurrentPosition().y() - turnstiles.get(0).getCenterPosition().y()) <= agent.getRadius()) {
//                            for (Turnstile turnstile : turnstiles) {
//                                if (turnstile.isPointInSegment(agent.getCurrentPosition()) || turnstile.minimumDistance(agent.getCurrentPosition()).getMagnitude() <= agent.getRadius()) {
//                                    System.out.println("+ Blocking turnstile");
//                                    turnstile.setBlockTime(t);
//                                    //agent.setTargetPoint(turnstile.getCenterPosition().add(new Point(0,Y_HORIZON)));
//                                    break;
//                                }
//                            }
//                        }

                if (!agent.hasEscaped(board.getDimL() + Y_HORIZON)) {
                    for (Board.Cell neighborCell : getBoard().getCells()) {
                        if (neighborCell.isInCell(agent.getCurrentPosition())) {
                            boardState.get(t).putIfAbsent(neighborCell, new ArrayList<>());
                            boardState.get(t).get(neighborCell).add(agent);
                            break;
                        }
                    }
                }
                else {
                    System.out.println("Agent: "+agent.getId()+" escaped");
                }
            });
            t += dt;
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
        boardState.put(t, new HashMap<>());
        List<Agent> newAgents = new ArrayList<>();
        for (int i = 0; i < maxAgents; i++) {
            Point initialPosition = Utils.getRandomPoint(i, PADDING*2, PADDING*2, board.getDimL()-PADDING*2, board.getDimL()/2, newAgents, maxD);
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
                    boardState.get(t).putIfAbsent(cell, new ArrayList<>());
                    boardState.get(t).get(cell).add(newAgent);
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
