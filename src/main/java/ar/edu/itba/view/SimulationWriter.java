package ar.edu.itba.view;

import ar.edu.itba.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationWriter {

    private static final double DELTA = 0.03;
    private static final double WALL_RADIUS = 0.05;

    private static Data drawWalls(List<Wall> walls, int idx) {
        StringBuilder wallsString = new StringBuilder();
        for (Wall wall : walls) {
            Point currentPoint, targetPoint;
            Vector step;
            if (wall.getStartPoint().getVector(wall.getEndPoint()).getAngle() < Math.PI) {
                currentPoint = wall.getStartPoint();
                targetPoint = wall.getEndPoint();
            } else {
                currentPoint = wall.getEndPoint();
                targetPoint = wall.getStartPoint();
            }
            step = new Vector(DELTA, currentPoint.getVector(targetPoint).getAngle());
            while (currentPoint.getVector(targetPoint).getAngle() < Math.PI) {
                wallsString.append(String.format("%d %.4f %.4f 0 0 %.4f 255 255 255\n", -idx, currentPoint.x(), currentPoint.y(), WALL_RADIUS));
                currentPoint = currentPoint.addVector(step);
                idx += 1;
            }
        }
        return new Data(
                wallsString.toString(),
                idx
        );
    }

    public static void writeSimulationOutput(PedestrianSimulation simulation, String filename) throws IOException {
        int idx = 1;
        Data dataWalls = drawWalls(simulation.getWalls(), idx);
        String structureString = dataWalls.dataString();
        idx = dataWalls.idx();

        //Map<Integer, Map<Board.Cell, List<Agent>>> boardState = simulation.getBoardState();
        try (FileWriter fw = new FileWriter(filename)) {
            for(int step = 0; step < simulation.getStep(); step++) {
                final int finalStep = step;
                Agent[] agents = simulation.getAgents().stream().filter(a -> a.getPositions().size() > finalStep).toArray(Agent[]::new);
                fw.write(String.format("%d\n\n", idx+agents.length-1));
                fw.write(structureString);
                for (Agent agent : agents) {
                    if (agent.getPositions().size() > step) {
                        fw.write(
                                String.format("%d %.4f %.4f %.4f %.4f %.4f 255 255 0\n",
                                        agent.getId(),
                                        agent.getPositionAt(step).x(),
                                        agent.getPositionAt(step).y(),
                                        agent.getVelocityAt(step).getVectorX(),
                                        agent.getVelocityAt(step).getVectorY(),
                                        agent.getRadius()
                                )
                        );
                    }
                }
            }
        }
    }

    public static void writeEscapeData(PedestrianSimulation simulation, String filename) throws IOException {

        double dt = simulation.getDt();
        int totalSteps = simulation.getStep();
        List<Agent> agents = simulation.getAgents();

        Map<Double, Integer> escapes = new HashMap<>();
        for (double time = 0; time < dt * totalSteps; time += dt) {
            double finalTime = time;
            escapes.put(
                        time,
                        (int) agents.stream()
                                    .filter(agent -> agent.getEscapeTime() > 0 && agent.getEscapeTime() <= finalTime)
                                    .count()
                    );
        }

        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("time;escaped\n");
            for (double time : escapes.keySet().stream().sorted().toList()) {
                fw.write(String.format("%.4f;%d\n", time, escapes.get(time)));
            }
        }
    }

    private record Data(String dataString, int idx) {}
}
