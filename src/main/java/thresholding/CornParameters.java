package thresholding;

class CornParameters {
    int[] squares;
    int[] heights;
    int[] widths;
    int[] thicknesses;

    CornParameters(int width) {
        squares = new int[width];
        heights = new int[width];
        widths = new int[width];
        thicknesses = new int[width];
    }
}
