package ar.edu.itba.model;

import java.util.List;
import java.util.Map;

public class Verlet {

        public static void step(Agent agent, Vector force, double dt) {
            Point newPosition = updatePosition(agent, force, dt);
            agent.setCurrentState(
                    new Agent.State(
                        newPosition,
                        updateVelocity(agent, force, dt, newPosition)
                    )
            );
        }

    private static Point updatePosition(Agent agent, Vector force, double dt) {
        Point currentPosition = agent.getCurrentPosition();
        Point previousPosition;
        if (agent.getPositions().size() == 1) {
            previousPosition = estimatePrevPosition(agent, force, dt);
        } else {
            previousPosition = agent.getPositionFromLast(1);
        }
        return currentPosition
                .multiplyByConstant(2)
                .subtract(previousPosition)
                .addVector(
                        force
                            .multiplyByConstant(dt*dt/agent.getMass())
                );
    }

    private static Vector updateVelocity(Agent agent, Vector force, double dt, Point newPosition) {
            Point previousPosition;
            if (agent.getPositions().size() == 1) {
                previousPosition = estimatePrevPosition(agent, force, dt);
            } else {
                previousPosition = agent.getPositionFromLast(1);
            }
            return newPosition
                    .subtract(previousPosition)
                    .multiplyByConstant(1/(2*dt))
                    .toVector();
    }

    private static Point estimatePrevPosition(Agent agent, Vector force, final double dt) {
        Point currentPosition = agent.getCurrentPosition();
        Vector currentVelocity = agent.getCurrentVelocity();
        return new Point(
                currentPosition.x() - dt * currentVelocity.getVectorX() + dt*dt * force.getVectorX() / (2*agent.getMass()),
                currentPosition.y() - dt * currentVelocity.getVectorY() + dt*dt * force.getVectorY() / (2*agent.getMass())
        );
    }

}
