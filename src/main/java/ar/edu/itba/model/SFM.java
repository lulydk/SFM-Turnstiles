package ar.edu.itba.model;

import java.util.List;

public class SFM {

    private final double kn;
    private final double kt;
    private final double A;
    private final double B;
    private final double tau;

    public SFM(int kn, int kt, double A, double B, double tau) {
        this.kn = kn;
        this.kt = kt;
        this.A = A;
        this.B = B;
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
                            .multiplyByConstant( A*Math.exp((radiiSum - agentsDistance)/B) + kn * g(radiiSum - agentsDistance) )
                            .add(
                                    tangentVector
                                            .multiplyByConstant( kt * g(radiiSum - agentsDistance) * tangentialVelocityDiff )
                            )
            );
        }

        Vector wallsForce = new Vector(0, 0);
        for (Wall wall : walls) {
            double distanceToWall = wall.minimumDistance(agent.getCurrentPosition());
            Vector directionToWall = wall.getPerpendicularDirection(agent.getCurrentPosition());
            Vector tangentVector = Vector.getTangentVector(directionToWall);

            wallsForce = wallsForce.add(
                    directionToWall
                            .multiplyByConstant( A*Math.exp((agent.getRadius() - distanceToWall)/B) - kn * g(agent.getRadius() - distanceToWall) )
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
