package ar.edu.itba.model;

import java.util.List;

public class SFM {

    private final double kn;
    private final double kt;
    private final double A_a;
    private final double A_w;
    private final double B_a;
    private final double B_w;
    private final double tau;

    public SFM(int kn, int kt, double A_a, double A_w, double B_a, double B_w, double tau) {
        this.kn = kn;
        this.kt = kt;
        this.A_a = A_a;
        this.A_w = A_w;
        this.B_a = B_a;
        this.B_w = B_w;
        this.tau = tau;
    }

    public Vector computeForce(Agent agent, List<Agent> neighbors, List<Wall> walls) {
        Vector desireForce = agent.getDesiredVelocity()
                .subtract(agent.getCurrentVelocity())
                .multiplyByConstant(agent.getMass()/tau);

        Vector particlesForce = new Vector(0, 0);
        for (Agent neighbor : neighbors) {
            double radiiSum = agent.getRadius() + neighbor.getRadius();
            double agentsDistance = agent.getCurrentPosition().distance(neighbor.getCurrentPosition());
            Vector directionToAgent = neighbor.getCurrentPosition().getVector(agent.getCurrentPosition()).normalizeVector();
            Vector tangentVector = Vector.getTangentVector(directionToAgent);
            double tangentialVelocityDiff = tangentVector.dotProduct(neighbor.getCurrentVelocity().subtract(agent.getCurrentVelocity()));

            particlesForce = particlesForce.add(
                    directionToAgent
                            .multiplyByConstant( A_a*Math.exp((radiiSum - agentsDistance)/B_a) + kn * g(radiiSum - agentsDistance) )
                            .add(
                                    tangentVector
                                            .multiplyByConstant( kt * g(radiiSum - agentsDistance) * tangentialVelocityDiff )
                            )
            );
        }

        Vector wallsForce = new Vector(0, 0);
        for (Wall wall : walls) {
            if(!wall.isInFront(agent.getCurrentPosition()))
                continue;
            Vector directionToWall = wall.minimumDistance(agent.getCurrentPosition());
            Vector tangentVector = Vector.getTangentVector(directionToWall);
            double distanceToWall = directionToWall.getMagnitude();

            wallsForce = wallsForce.add(
            directionToWall
                    .multiplyByConstant( A_w*Math.exp((agent.getRadius() - distanceToWall)/B_w) - kn * g(agent.getRadius() - distanceToWall) )
                    .subtract(
                            tangentVector
                                    .multiplyByConstant( kt * g(agent.getRadius() - distanceToWall) * agent.getCurrentVelocity().dotProduct(tangentVector) )
                    )
            );
        }

        return desireForce.add(particlesForce).add(wallsForce);
    }

    private static double g(double x) {
        return x < 0 ? 0:x;
    }
}
