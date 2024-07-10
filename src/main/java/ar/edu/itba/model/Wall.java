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
        return minimumDistance(point) < DELTA;
    }

    /**
     * Calculates the minimum distance from a point to the Wall.
     *
     * @param  point the point to calculate the minimum distance
     * @return the minimum distance from the point to the wall
     *
     * based on: <a href="https://www.geeksforgeeks.org/minimum-distance-from-a-point-to-the-line-segment-using-vectors/">Minimum distance from point to line segment</a>
     */
    public double minimumDistance(Point point) {
        double startX = startPoint.x();
        double startY = startPoint.y();
        double endX = endPoint.x();
        double endY = endPoint.y();
        double pointX = point.x();
        double pointY = point.y();

        double wallX = endX - startX;
        double wallY = endY - startY;
        double pointToEndX = pointX - endX;
        double pointToEndY = pointY - endY;
        double pointToStartX = pointX - startX;
        double pointToStartY = pointY - startY;

        double dotProductEnd = wallX * pointToEndX + wallY * pointToEndY;
        double dotProductStart = wallX * pointToStartX + wallY * pointToStartY;

        double minDistance;
        if (dotProductEnd > 0) {
            double distanceX = pointX - endX;
            double distanceY = pointY - endY;
            minDistance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        } else if (dotProductStart < 0) {
            double distanceX = pointX - startX;
            double distanceY = pointY - startY;
            minDistance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        } else {
            double wallModulus = Math.sqrt(wallX * wallX + wallY * wallY);
            minDistance = Math.abs(wallX * pointToStartY - wallY * pointToStartX) / wallModulus;
        }
        return minDistance;
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
        return a.subtract(bHat.multiplyByConstant(bHat.dotProduct(a)));
    }

    @Override
    public String toString() {
        return "Wall [startPoint=" + startPoint + ", endPoint=" + endPoint + "]";
    }

}
