package ar.edu.itba.model;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final double dimL;
    private final double dimC;
    private final int cellCount;
    private List<Cell> cells;


    public Board(double dimL, int cellLineCount) {
        this.dimL = dimL;
        this.dimC = dimL / cellLineCount;
        this.cellCount = cellLineCount*cellLineCount;
        generateCells(cellLineCount);
    }

    public double getDimL() {
        return dimL;
    }

    public double getDimC() {
        return dimC;
    }

    public List<Cell> getCells() {
        return cells;
    }

    private void generateCells(int cellLineCount) {
        cells = new ArrayList<>(cellCount);
        double currX = 0, currY = 0;
        for (int row = 0; row < cellLineCount; row++) {
            for (int col = 0; col < cellLineCount; col++) {
                cells.add(new Cell(
                        row,
                        col,
                        currX, currX+dimC,
                        currY, currY+dimC
                ));
                currX += dimC;
            }
            currX = 0;
            currY += dimC;
        }
        cells.forEach(Cell::findNeighborCells);
    }

    @Override
    public String toString() {
        String boardString = String.format("Board(dimL=%.2f, cellCount=%d, cells=[ ", dimL, cellCount);
        for (Cell cell : cells) {
            boardString = boardString.concat(cell.toString()).concat("; ");
        }
        boardString = boardString.concat("])");
        return boardString;
    }


    public class Cell {
        private final int row;
        private final int col;
        private final double[] rangeX;
        private final double[] rangeY;
        private List<Cell> neighbors;

        public Cell(int row, int col, double rangeXstart, double rangeXend, double rangeYstart, double rangeYend) {
            this.row = row;
            this.col = col;
            this.rangeX = new double[]{rangeXstart,rangeXend};
            this.rangeY = new double[]{rangeYstart,rangeYend};
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public List<Cell> getNeighbors() {
            return neighbors;
        }

        public boolean isInCell(Point p) {
            return  p.x() >= rangeX[0] && p.x() < rangeX[1]
                    && p.y() >= rangeY[0] && p.y() < rangeY[1];
        }

        private void findNeighborCells() {
            this.neighbors = new ArrayList<>();
            for (Cell cell : cells) {
                if (!(cell.col > col+1 || cell.col < col-1 || cell.row > row+1 || cell.row < row-1) && !(cell.row == row && cell.col == col)) {
                    neighbors.add(cell);
                }
            }
        }

        @Override
        public String toString() {
            return String.format("Cell(row=%d, col=%d, rangeX=[%.2f, %.2f], rangeY=[%.2f, %.2f])", row, col, rangeX[0], rangeX[1], rangeY[0], rangeY[1]);
        }
    }

}
