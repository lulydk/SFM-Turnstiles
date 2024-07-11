package ar.edu.itba.model;

import java.util.Random;

public class Turnstile extends Wall {
    private final int id;
    private final double width;
    private final Point centerPosition;
    private final Corridor corridor;

    private double queue;
    private double unlockTime;
    //private boolean locked;
    private final double LAMBDA = 1/5.0;

    public Turnstile(int id, Point position, double width, double corridorLength) {
        super(new Point(position.x() - width/2, position.y()), new Point(position.x() + width/2, position.y()));
        this.id = id;
        this.width = width;
        this.centerPosition = position;

        corridor = new Corridor(corridorLength, new Point(position.x(), position.y() - corridorLength));
        queue = 0;
        //blocked = false;
    }

    public boolean isAligned(Point point, double radius) {
        return point.x() > (centerPosition.x() - width/2) && point.x() < (centerPosition.x() + width/2) &&
                (corridor.isInFront(point) || corridor.minimumDistance(point).getMagnitude() < radius);
    }

    public int getId() {
        return id;
    }

    public double getWidth() {
        return width;
    }

    public double getCorridorLength() {
        return corridor.getCorridorLength();
    }

    public Point getCenterPosition() {
        return centerPosition;
    }

    public Point getCorridorCenterPosition() {
        return corridor.getCorridorCenterPosition();
    }

    public Corridor getCorridor() {
        return corridor;
    }

    public double getQueue() {
        return queue;
    }

    public boolean isBlocked(double currentTime) {
        return currentTime >= unlockTime;
    }

    /*
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

     */

    public void setBlockTime(double currentTime) {
        //unlockTime = currentTime - Math.log(1 - new Random().nextDouble()) / LAMBDA;
        unlockTime = currentTime + 100.0;
        //setLocked(true);
    }

    public void addToQueue(double count) {
        queue += count;
    }

    public double removeFromQueue(double count) {
        queue -= count;
        return queue;
    }

    public class Corridor extends Wall {

        private final double corridorLength;
        private final Point corridorCenterPosition;

        public Corridor(double corridorLength, Point corridorCenterPosition) {
            super(new Point(corridorCenterPosition.x() - width/2, corridorCenterPosition.y()), new Point(corridorCenterPosition.x() + width/2, corridorCenterPosition.y()));
            this.corridorLength = corridorLength;
            this.corridorCenterPosition = corridorCenterPosition;
        }

        public double getCorridorLength() {
            return corridorLength;
        }

        public Point getCorridorCenterPosition() {
            return corridorCenterPosition;
        }

    }
}
