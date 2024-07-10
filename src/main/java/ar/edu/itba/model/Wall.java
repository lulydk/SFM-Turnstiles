package ar.edu.itba.model;

public class Wall {
    private Point startPoint;
    private Point endPoint;
    private static final double DELTA = 0.000001;

    public Wall(Point startPoint, Point endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }

    public boolean isPointInSegment(Point point) {
        return minimumDistance(point).getMagnitude() < DELTA;
    }

    /**
     * Calculates the minimum distance from a point to the Wall.
     *
     * @param  point the point to calculate the minimum distance
     * @return the minimum distance from the point to the wall
     *
     * based on: <a href="https://www.geeksforgeeks.org/minimum-distance-from-a-point-to-the-line-segment-using-vectors/">Minimum distance from point to line segment</a>
     */
    public Vector minimumDistance(Point point) {
        Vector wall = startPoint.getVector(endPoint);
        Vector startToPoint = startPoint.getVector(point);
        Vector endToPoint = endPoint.getVector(point);

        if(wall.dotProduct(endToPoint) > 0)
            return endToPoint;
        else if(wall.dotProduct(startToPoint) < 0)
            return startToPoint;
        else
            return getPerpendicularDirection(point);
    }

    /**
     * Calculates the perpendicular direction between a point and the wall
     *
     * @param point the point to calculate the direction from
     * @return the perpendicular vector
     *
     * based on <a href="https://en.wikipedia.org/wiki/Vector_projection#Vector_rejection_2">Vector rejection</a>
     */
    public Vector getPerpendicularDirection(Point point) {
        Vector a = startPoint.getVector(point);
        Vector bHat = endPoint.getVector(startPoint).normalizeVector();
        Vector proj = bHat.multiplyByConstant(bHat.dotProduct(a));
        return a.subtract(proj);
    }

    public boolean isInFront(Point point) {
        Vector normal = Vector.getTangentVector(startPoint.getVector(endPoint));
        Vector direction = startPoint.getVector(point);

        return normal.dotProduct(direction) >= 0;
    }

    @Override
    public String toString() {
        return "Wall [startPoint=" + startPoint + ", endPoint=" + endPoint + "]";
    }

}
