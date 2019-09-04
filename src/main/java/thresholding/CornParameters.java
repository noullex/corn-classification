package thresholding;

class CornParameters {
    int[] squares;
    int[] heights;
    int[] width;

    CornParameters(int width) {
        squares = new int[width];
        heights = new int[width];
        this.width = new int[width];
    }
}
