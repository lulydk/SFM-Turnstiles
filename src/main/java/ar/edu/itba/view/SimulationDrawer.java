package ar.edu.itba.view;

import ar.edu.itba.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SimulationDrawer {

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

    private static Data drawTurnstiles(List<Turnstile> turnstiles, int idx) {
        StringBuilder turnstilesString = new StringBuilder();
        for (Turnstile turnstile : turnstiles) {
            turnstilesString.append(String.format("%d %.4f %.4f 0 0 %.4f 128 128 128\n", -idx, turnstile.getStartPoint().x(), turnstile.getStartPoint().y(), WALL_RADIUS));
            idx += 1;
            turnstilesString.append(String.format("%d %.4f %.4f 0 0 %.4f 128 128 128\n", -idx, turnstile.getEndPoint().x(), turnstile.getEndPoint().y(), WALL_RADIUS));
            idx += 1;
            Turnstile.Corridor corridor = turnstile.getCorridor();
            turnstilesString.append(String.format("%d %.4f %.4f 0 0 %.4f 128 128 128\n", -idx, corridor.getStartPoint().x(), corridor.getStartPoint().y(), WALL_RADIUS));
            idx += 1;
            turnstilesString.append(String.format("%d %.4f %.4f 0 0 %.4f 128 128 128\n", -idx, corridor.getEndPoint().x(), corridor.getEndPoint().y(), WALL_RADIUS));
            idx += 1;
            /*
            Point currentPointLeft = turnstile.getStartPoint();
            Point currentPointRight = turnstile.getEndPoint();
            Vector step = new Vector(DELTA, currentPointLeft.getVector(corridor.getStartPoint()).getAngle());
            // TODO: Fix this cut condition
            while (currentPointLeft != corridor.getStartPoint()) {
                turnstilesString.append(String.format("%d %.4f %.4f 0 0 %.4f 128 128 128)\n", idx, currentPointLeft.x(), currentPointLeft.y(), WALL_RADIUS));
                currentPointLeft = currentPointLeft.addVector(step);
                idx += 1;
                turnstilesString.append(String.format("%d %.4f %.4f 0 0 %.4f 128 128 128)\n", idx, currentPointRight.x(), currentPointRight.y(), WALL_RADIUS));
                currentPointRight = currentPointRight.addVector(step);
                idx += 1;
            }
            */
        }
        return new Data(
                turnstilesString.toString(),
                idx
        );
    }

    public static void drawSimulation(PedestrianSimulation simulation) throws IOException {
        int idx = 1;
        Data dataWalls = drawWalls(simulation.getWalls(), idx);
        String structureString = dataWalls.dataString();
        idx = dataWalls.idx();
        Data dataTurnstiles = drawTurnstiles(simulation.getTurnstiles(), idx);
        structureString = structureString.concat(dataTurnstiles.dataString());
        idx = dataTurnstiles.idx();

        Map<Double, Map<Board.Cell, List<Agent>>> boardState = simulation.getBoardState();
        try (FileWriter fw = new FileWriter("particles.xyz")) {
            for(int step = 0; step < boardState.size(); step++) {
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

    private record Data(String dataString, int idx) {}
}
