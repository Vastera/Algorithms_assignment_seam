/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    private Picture currentPic;
    private double[][] currentEnergy;
    private boolean isTransposed;


    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null)
            throw new IllegalArgumentException("input picture is null!");
        currentPic = picture;
        isTransposed = false;
        currentEnergy = new double[width()][height()];
        for (int x = 0; x < width(); x++)
            for (int y = 0; y < height(); y++)
                currentEnergy[x][y] = energy(x, y);
    }

    // current picture
    public Picture picture() {
        return currentPic;
    }

    // width of current picture
    public int width() {
        return currentPic.width();
    }

    // height of current picture
    public int height() {
        return currentPic.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x > width() - 1 || y < 0 || y > height() - 1)
            throw new IllegalArgumentException("x or y is out of range~");
        if (x == 0 || y == 0 || x == width() - 1 || y == height() - 1)
            return 1000;

        return Math.sqrt(squareSum(currentPic.getRGB(x - 1, y), currentPic.getRGB(x + 1, y))
                                 + squareSum(
                currentPic.getRGB(x, y - 1), currentPic.getRGB(x, y + 1)));

    }

    private double squareSum(int c1, int c2) {
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = (c1) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = (c2) & 0xFF;
        return square(r1 - r2) + square(g1 - g2) + square(b1 - b2);
    }

    private double square(int x) {
        return x * x;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        if (isTransposed) {
            currentEnergy = transpose(currentEnergy);
            isTransposed = false;
        }
        int h = height();
        int w = width();
        return findSeam(w, h);
    }

    private int[] findSeam(int w, int h) {
        double[] distTo = new double[h];
        double[] newDistTo = new double[h];
        int[][] edgeTo = new int[w][h];
        for (int y = 0; y < h; y++)
            distTo[y] = 1000.0; // first column
        for (int x = 1; x < w; x++) {
            // first row y=0
            if (distTo[0] < distTo[1]) {
                newDistTo[0] = distTo[0];
                edgeTo[x][0] = 0;
            }
            else {
                newDistTo[0] = distTo[1];
                edgeTo[x][0] = 1;
            }
            newDistTo[0] = newDistTo[0] + currentEnergy[x][0];
            // second to last - row
            for (int y = 1; y < h - 1; y++) {
                if (distTo[y - 1] < distTo[y]) {
                    if (distTo[y - 1] < distTo[y + 1]) {
                        newDistTo[y] = distTo[y - 1];
                        edgeTo[x][y] = y - 1;
                    }
                    else {
                        newDistTo[y] = distTo[y + 1];
                        edgeTo[x][y] = y + 1;
                    }
                }
                else {
                    if (distTo[y] < distTo[y + 1]) {
                        newDistTo[y] = distTo[y];
                        edgeTo[x][y] = y;
                    }
                    else {
                        newDistTo[y] = distTo[y + 1];
                        edgeTo[x][y] = y + 1;
                    }
                }
                newDistTo[y] = newDistTo[y] + currentEnergy[x][y];
            }
            // last row y=h-1
            if (distTo[h - 2] < distTo[h - 1]) {
                newDistTo[h - 1] = distTo[h - 2];
                edgeTo[x][h - 1] = h - 2;
            }
            else {
                newDistTo[h - 1] = distTo[h - 1];
                edgeTo[x][h - 1] = h - 1;
            }
            newDistTo[h - 1] = newDistTo[h - 1] + currentEnergy[x][h - 1];
            // copy newDistTo to distTo
            System.arraycopy(newDistTo, 0, distTo, 0, h);
        }
        double minEnergySum = distTo[0];
        int minIndice = 0;
        for (int y = 1; y < h; y++) {
            if (distTo[y] < minEnergySum) {
                minIndice = y;
            }
        }
        int[] shortestPath = new int[w];
        for (int x = w - 1; x >= 0; x--) {
            shortestPath[x] = minIndice;
            minIndice = edgeTo[x][minIndice];
        }
        return shortestPath;
    }

    private double[][] transpose(double[][] a) {
        double[][] t = new double[a[0].length][a.length];
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[0].length; j++)
                t[j][i] = a[i][j];
        return t;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        if (!isTransposed) {
            currentEnergy = transpose(currentEnergy);
            isTransposed = true;
        }
        return findSeam(height(), width());
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null)
            throw new IllegalArgumentException("input seam is null~");

        int w = width();
        int h = height();

        if (h <= 1)
            throw new IllegalArgumentException(
                    "cannot remove vertical seam, because the height of the picture less than or equals 1~");
        if (seam.length != w)
            throw new IllegalArgumentException("the seam length doesn't equal the picture width");
        for (int i = 0; i < w - 1; i++)
            if (Math.abs(seam[i + 1] - seam[i]) > 1)
                throw new IllegalArgumentException(
                        "seam line is invalid, because the difference between adjacent points is bigger than 1~");
        if (isTransposed) {
            currentEnergy = transpose(currentEnergy);
            isTransposed = false;
        }
        Picture newPic = new Picture(w, h - 1);
        double[][] newEnergy = new double[w][h - 1];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < seam[x]; y++) {
                newPic.setRGB(x, y, currentPic.getRGB(x, y));
                newEnergy[x][y] = currentEnergy[x][y];
            }
            for (int y = seam[x]; y < h - 1; y++) {
                newPic.setRGB(x, y, currentPic.getRGB(x, y + 1));
                newEnergy[x][y] = currentEnergy[x][y + 1];
            }
        }
        currentPic = newPic;
        for (int x = 0; x < w; x++) {
            if (seam[x] > 0)
                newEnergy[x][seam[x] - 1] = energy(x, seam[x] - 1);
            if (seam[x] < h - 1)
                newEnergy[x][seam[x]] = energy(x, seam[x]);
        }
        currentEnergy = newEnergy;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null)
            throw new IllegalArgumentException("input seam is null~");
        int w = width();
        int h = height();
        if (w <= 1)
            throw new IllegalArgumentException(
                    "cannot remove vertical seam, because the width of the picture less than or equals 1~");
        if (seam.length != h)
            throw new IllegalArgumentException("the seam length doesn't equal the picture height~");
        for (int i = 0; i < h - 1; i++)
            if (Math.abs(seam[i + 1] - seam[i]) > 1)
                throw new IllegalArgumentException(
                        "seam line is invalid, because the difference between adjacent points is bigger than 1~");
        if (isTransposed) {
            currentEnergy = transpose(currentEnergy);
            isTransposed = false;
        }
        Picture newPic = new Picture(w - 1, h);
        double[][] newEnergy = new double[w - 1][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < seam[y]; x++) {
                newPic.setRGB(x, y, currentPic.getRGB(x, y));
                newEnergy[x][y] = currentEnergy[x][y];
            }
            for (int x = seam[y]; x < w - 1; x++) {
                newPic.setRGB(x, y, currentPic.getRGB(x + 1, y));
                newEnergy[x][y] = currentEnergy[x + 1][y];
            }
        }
        currentPic = newPic;
        for (int y = 0; y < h; y++) {
            if (seam[y] > 0)
                newEnergy[seam[y] - 1][y] = energy(seam[y] - 1, y);
            if (seam[y] < w - 1)
                newEnergy[seam[y]][y] = energy(seam[y], y);
        }
        currentEnergy = newEnergy;
    }

    public static void main(String[] args) {


    }
}
