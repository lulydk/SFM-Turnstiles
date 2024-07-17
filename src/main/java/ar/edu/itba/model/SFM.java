package ar.edu.itba.model;

import java.util.ArrayList;
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
        if (agent.getAdvancement() != Agent.Advancement.ON_TURNSTILE || agent.getAdvancement() != Agent.Advancement.ON_TRANSACTION) {
            for (Agent neighbor : neighbors) {
                double radiiSum = agent.getRadius() + neighbor.getRadius();
                double agentsDistance = agent.getCurrentPosition().distance(neighbor.getCurrentPosition());
                Vector directionToAgent = neighbor.getCurrentPosition().getVector(agent.getCurrentPosition()).normalizeVector();
                Vector tangentVector = Vector.getTangentVector(directionToAgent);
                double tangentialVelocityDiff = tangentVector.dotProduct(neighbor.getCurrentVelocity().subtract(agent.getCurrentVelocity()));
                double superposition = radiiSum - agentsDistance;

                particlesForce = particlesForce.add(
                        directionToAgent
                                .multiplyByConstant( A_a*Math.exp(superposition/B_a) + kn * g(superposition) )
                                .add(
                                        tangentVector
                                                .multiplyByConstant( kt * g(superposition) * tangentialVelocityDiff )
                                )
                );
            }
        }

        Vector wallsForce = new Vector(0, 0);

        List<Wall> delimiters = new ArrayList<>(walls);
        if (agent.getTargetTurnstile().isLocked() && agent.getAdvancement() != Agent.Advancement.ON_TURNSTILE) {
            delimiters.add(new Wall(agent.getTargetTurnstile().getEndPoint(), agent.getTargetTurnstile().getStartPoint()));
        }

        for (Wall wall : delimiters) {
            if(!wall.isInFront(agent.getCurrentPosition()))
                continue;
            Vector directionToWall = wall.minimumDistance(agent.getCurrentPosition());
            Vector tangentVector = Vector.getTangentVector(directionToWall);
            double superposition = agent.getRadius() - directionToWall.getMagnitude();

            wallsForce = wallsForce.add(
            directionToWall
                    .multiplyByConstant( A_w*Math.exp((superposition)/B_w) + kn * g(superposition) )
                    .subtract(
                            tangentVector
                                    .multiplyByConstant( kt * g(superposition) * agent.getCurrentVelocity().dotProduct(tangentVector) )
                    )
            );
        }

        return desireForce.add(particlesForce).add(wallsForce);
    }

    private static double g(double x) {
        return x < 0 ? 0:x;
    }
}
