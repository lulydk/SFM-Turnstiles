package ar.edu.itba.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Agent {

    // fixed
    private final int id;
    private final double mass;
    private final double radius;

    // updatable
    private int idx;
    private Point targetPoint;
    private Vector desiredVelocity;
    private Turnstile targetTurnstile;
    private final List<Point> positions;
    private final List<Vector> velocities;
    private final double desiredSpeed;

    private Advancement advancement;

    public enum Advancement {
        DEFAULT,
        ON_CORRIDOR,
        ON_TURNSTILE,
        ON_TRANSACTION,
        FINISHED_TRANSACTION
    }

    public Agent(int id, double mass, double radius, Point initialPosition, Vector initialVelocity, Turnstile targetTurnstile) {
        this.id = id;
        this.mass = mass;
        this.radius = radius;

        idx = -1;
        positions = new ArrayList<>();
        velocities = new ArrayList<>();
        desiredSpeed = initialVelocity.getMagnitude();

        advancement = Advancement.DEFAULT;

        setCurrentState(new State(initialPosition,initialVelocity));
        setTargetPoint(targetTurnstile.getCorridorCenterPosition().add(new Point(0, 0.8)));
        this.targetTurnstile = targetTurnstile;
    }

    public void setCurrentState(State state) {
        //System.out.println("setting state for agent id " + id);
        idx++;
        positions.add(state.currentPosition());
        velocities.add(state.currentVelocity());
    }

    public int getId() {
        return id;
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    public Point getPositionFromLast(int position) {
        return positions.get(idx-position);
    }

    public Point getCurrentPosition() {
        return positions.get(idx);
    }

    public List<Vector> getVelocities() {
        return velocities;
    }

    public List<Point> getPositions() {
        return positions;
    }

    public Vector getDesiredVelocity() {
        return desiredVelocity;
    }

    public Vector getCurrentVelocity() {
        return velocities.get(idx);
    }

    public void setDesiredVelocity(Vector desiredVelocity) {
        this.desiredVelocity = desiredVelocity;
    }

    public Point getTargetPoint() {
        return targetPoint;
    }

    public void setTargetPoint(Point targetPoint) {
        if(this.targetPoint == targetPoint)
            return;
        //System.out.println("-- Updating Agent "+id+" target");
        this.targetPoint = targetPoint;
        setDesiredVelocity(new Vector(
                desiredSpeed,
                getCurrentPosition().getVector(targetPoint).getAngle()
        ));
    }

    public Turnstile getTargetTurnstile() {
        return targetTurnstile;
    }

    public void setTargetTurnstile(Turnstile targetTurnstile) {
        this.targetTurnstile = targetTurnstile;
    }

    public boolean hasEscaped(double yCoord) {
        return getCurrentPosition().y() > yCoord;
    }

    public Point getPositionAt(int i) {
        return positions.get(i);
    }

    public Vector getVelocityAt(int i) {
        return velocities.get(i);
    }

    public Advancement getAdvancement() {
        return advancement;
    }

    public void setAdvancement(Advancement advancement) {
        this.advancement = advancement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return getId() == agent.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public record State(Point currentPosition, Vector currentVelocity) {}
}
