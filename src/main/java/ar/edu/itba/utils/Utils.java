package ar.edu.itba.utils;

import ar.edu.itba.model.Agent;
import ar.edu.itba.model.Point;

import java.util.List;
import java.util.Random;

public class Utils {

    public final static String METHOD_CLOSENESS = "closeness";
    public final static String METHOD_EMPTINESS = "emptiness";
    private final static double DELTA = 0.0001;
    private final static long MULTIPLIER = 100000;

    private final static Random random = new Random(11);

    public static double getUniformRadius(long seed, double minDiameter, double maxDiameter) {
        return (minDiameter / 2) + (maxDiameter / 2 - minDiameter / 2) * random.nextDouble();
    }

    public static Point getRandomPoint(long seed, double minX, double minY, double maxX, double maxY, List<Agent> agents, double minDistance) {
        //Random random = new Random();
        boolean overlaps = true;
        Point newPoint = new Point(
                minX + (maxX - minX) * random.nextDouble() + DELTA,
                minY + (maxY - minY) * random.nextDouble() + DELTA
        );
        while (overlaps) {
            overlaps = false;
            for (Agent agent : agents) {
                if (newPoint.distance(agent.getCurrentPosition()) < minDistance) {
                    overlaps = true;
                    newPoint = new Point(
                            minX + (maxX - minX) * random.nextDouble() + DELTA,
                            minY + (maxY - minY) * random.nextDouble() + DELTA
                    );
                    break;
                }
            }
        }
        return newPoint;
    }

    public static double getRandomTransactionTime() {
        double deviation = 1.0;
        double mean = 5;
        return random.nextGaussian() * deviation + mean;
    }
}
