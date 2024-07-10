package ar.edu.itba.model;

/**
 * Represents a 2D point.
 **/
public record Point(double x, double y) {

    private static final double DELTA = 0.000001;

    /**
     * Returns the x-coordinate of the point.
     *
     * @return the x-coordinate of the point
     */
    @Override
    public double x() {
        return x;
    }

    /**
     * Returns the y-coordinate of the point.
     *
     * @return the y-coordinate of the point
     */
    @Override
    public double y() {
        return y;
    }

    /**
     * Adds the coordinates of the given Point to the current Point and returns a new Point with the sum.
     *
     * @param otherPoint the Point to add to the current Point
     * @return a new Point with the sum of the coordinates of the two Points
     */
    public Point add(Point otherPoint) {
        return new Point(x + otherPoint.x, y + otherPoint.y);
    }

    /**
     * Subtracts the coordinates of the given Point from the current Point and returns a new Point with the difference.
     *
     * @param otherPoint the Point to subtract from the current Point
     * @return a new Point with the difference of the coordinates of the two Points
     */
    public Point subtract(Point otherPoint) {
        return new Point(x - otherPoint.x, y - otherPoint.y);
    }

    /**
     * Calculates the Euclidean distance between this Point and another Point.
     *
     * @param  otherPoint the other Point to calculate the distance to
     * @return the Euclidean distance between the two Points
     */
    public double distance(Point otherPoint) {
        return Math.sqrt(Math.pow(x - otherPoint.x, 2) + Math.pow(y - otherPoint.y, 2));
    }

    /**
     * Calculates the vector from this Point to another Point.
     *
     * @param  otherPoint the other Point to calculate the vector to
     * @return  a new Vector object representing the vector from this Point to the other Point
     */
    public Vector getVector(Point otherPoint) {
        double dx = otherPoint.x - x;
        double dy = otherPoint.y - y;
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);
        return new Vector(magnitude, angle);
    }

    /**
     * Adds a vector to the current point and returns a new point with the sum of the coordinates.
     *
     * @param vector the vector to add to the current point
     * @return  a new point with the sum of the coordinates of the current point and the vector
     */
    public Point addVector(Vector vector) {
        return new Point(
            x + vector.getMagnitude() * Math.cos(vector.getAngle()), 
            y + vector.getMagnitude() * Math.sin(vector.getAngle())
        );
    }

    public Point multiplyByConstant(double constant) {
        return new Point(
                x * constant,
                y * constant
        );
    }

    public Vector toVector() {
        return new Vector(Math.sqrt(x * x + y * y), Math.atan2(y, x));
    }

    /**
     * Checks if this Point object is equal to another object.
     *
     * @param obj the object to compare to
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Point point)) {
            return false;
        }
        return Math.abs(point.x - x) <= DELTA && Math.abs(point.y - y) <= DELTA;
    }

    /**
     * Returns a string representation of the Point object.
     *
     * @return a string representation of the Point object
     */
    @Override
    public String toString() {
        return String.format("Point(x=%.4f, y=%.4f)", x, y);
    }
}
