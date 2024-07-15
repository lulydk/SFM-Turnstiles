package ar.edu.itba.model;

/**
 * Represents a 2D vector.
 **/
public class Vector {
    private double magnitude;
    private double angle;
    public static final Vector ZERO = new Vector(0,0);

    public Vector(double magnitude, double angle) {
        setAngle(angle);
        setMagnitude(magnitude);
    }

    public Vector(Vector anotherVector) {
        setMagnitude(anotherVector.getMagnitude());
        setAngle(anotherVector.getAngle());
    }

    /**
     * Gets the magnitude of the vector.
     *
     * @return  the magnitude of the vector
     */
    public double getMagnitude() {
        return magnitude;
    }

    /**
     * Gets the angle of the vector.
     *
     * @return  the angle of the vector
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Sets the magnitude of the vector.
     *
     * @param  newMagnitude  the new magnitude of the vector. Must be non-negative.
     * @throws IllegalStateException if the magnitude is negative.
     */
    public void setMagnitude(double newMagnitude) {
        if (newMagnitude < 0) {
            this.angle += Math.PI;
            this.magnitude = - newMagnitude;
            //throw new IllegalArgumentException("Magnitude cannot be negative");
        } else {
            this.magnitude = newMagnitude;
        }
    }

    /**
     * Sets the angle of the vector.
     *
     * @param angle the new angle of the vector in radians
     */
    public void setAngle(double angle) {
        angle = normalizeAngle(angle);
        this.angle = angle;
    }

    /**
     * Normalize the given angle to the range from 0 to 2PI.
     *
     * @param angle the angle in radians to be normalized
     * @return the normalized angle in radians
     */
    private double normalizeAngle(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle > 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

    /**
     * Adds the given vector to this vector and returns the result.
     *
     * @param otherVector the vector to be added to this vector
     * @return a new Vector object representing the result of the addition
     */
    public Vector add(Vector otherVector) {
        double newMagnitude = Math.sqrt(
                Math.pow(magnitude, 2) + Math.pow(otherVector.magnitude, 2) + 2 * magnitude * otherVector.magnitude * Math.cos(otherVector.angle - angle)
        );
        double newAngle = angle + Math.atan2(
                otherVector.magnitude * Math.sin(otherVector.angle - angle),
                magnitude + otherVector.magnitude * Math.cos(otherVector.angle - angle)
        );
        return new Vector(newMagnitude, newAngle);
    }

    /**
     * Subtracts the given vector from this vector and returns the result.
     *
     * @param  otherVector  the vector to be subtracted from this vector
     * @return  a new Vector object representing the result of the subtraction
     */
    public Vector subtract(Vector otherVector) {
        double newMagnitude = Math.sqrt(
                Math.pow(magnitude, 2) + Math.pow(otherVector.magnitude, 2) - 2 * magnitude * otherVector.magnitude * Math.cos(angle - otherVector.angle)
        );
        double newAngle = angle - Math.atan2(
            otherVector.magnitude * Math.sin(otherVector.angle - angle),
            magnitude - otherVector.magnitude * Math.cos(otherVector.angle - angle)
        );
        return new Vector(newMagnitude, newAngle);
    }

    /**
     * Normalizes the vector.
     */
    public Vector normalizeVector() {
        return new Vector(1, angle);
    }

    /**
     * Multiplies the magnitude of the vector by a given constant.
     *
     * @param  constant  the constant value to multiply the vector by
     */
    public Vector multiplyByConstant(double constant) {
        return new Vector(
                magnitude * constant,
                angle
        );
    }

    public Point toPoint() {
        return new Point(
                (Math.cos(angle) * magnitude),
                (Math.sin(angle) * magnitude));
    }

    /*
    public Vector crossProduct(Vector otherVector) {
        return new Vector(
            magnitude * otherVector.magnitude * Math.sin(angle - otherVector.angle),
            
        )
    }
     */

    public double dotProduct(Vector otherVector) {
        return magnitude * otherVector.magnitude * Math.cos(angle - otherVector.angle);
    }

    public double getVectorX() {
        return magnitude * Math.cos(angle);
    }

    public double getVectorY() {
        return magnitude * Math.sin(angle);
    }


    public static Vector getTangentVector(Vector directionVector) {
        double vX = directionVector.getVectorY() * -1;
        double vY = directionVector.getVectorX();
        return new Vector(
                Math.sqrt(Math.pow(vX, 2) + Math.pow(vY, 2)),
                Math.atan2(vY, vX)
        );
    }

    /**
     * Checks if this Vector object is equal to another object.
     *
     * @param  obj   the object to compare to
     * @return  true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector vector) {
            return Double.compare(vector.magnitude, magnitude) == 0 && Double.compare(vector.angle, angle) == 0;
        }
        return false;
    }
    
    /**
     * Returns a string representation of the Vector object.
     *
     * @return a string representation of the Vector object
     */
    @Override
    public String toString() {
        return String.format("Vector(magnitude=%.4f, angle=%.4f)", magnitude, angle);
    }
}
