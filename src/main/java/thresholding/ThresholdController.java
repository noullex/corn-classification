package thresholding;

import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.Constants.*;

public class ThresholdController {

    public void sort() throws IOException {
        BufferedImage image = ImageIO.read(Utils.getFileFromResources(TEST_DATA_FOLDER + "buckwheat_and_barley_1_2.bmp"));
        BufferedImage background = ImageIO.read(Utils.getFileFromResources(BACKGROUND_FOLDER + BACKGROUND_IMAGE));

//        int width = image.getWidth();
//        int height = image.getHeight();
//        byte[][] binaryImage = Binarizer.binarize(Binarizer.subtract(background, image));
        int width = 24;
        int height = 8;
        byte[][] binaryImage = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };

        CornParameters params = new CornParameters(width);
        List<Integer> previousLeftBorders = new ArrayList<>();
        List<Integer> previousRightBorders = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            List<Integer> currentLeftBorders = new ArrayList<>();
            List<Integer> currentRightBorders = new ArrayList<>();
            for (int x = 0; x < width - 1; x++) {
                if ((binaryImage[y][x] + binaryImage[y][x + 1]) * binaryImage[y][x + 1] == 1) {
                    currentLeftBorders.add(x + 1);
                }
                if ((binaryImage[y][x] + binaryImage[y][x + 1]) * binaryImage[y][x] == 1) {
                    currentRightBorders.add(x);
                }
            }
            calculateCornParams(currentLeftBorders, currentRightBorders, previousLeftBorders, previousRightBorders, params);
            contourAnalysis(binaryImage[y], previousLeftBorders, previousRightBorders, params,
                    20, 35,
                    3, 7,
                    4, 7,
                    5, 9);
            previousLeftBorders = currentLeftBorders;
            previousRightBorders = currentRightBorders;
        }
    }

    private void calculateCornParams(List<Integer> currentLeftBorders, List<Integer> currentRightBorders,
                                     List<Integer> previousLeftBorders, List<Integer> previousRightBorders,
                                     CornParameters params) {
        CornParameters tmpParams = new CornParameters(24);
        for (int i = 0; i < previousLeftBorders.size(); i++) {
            int currentSquare = 0;
            int currentHeight = 0;
            int currentWidth = 0;
            int currentThickness = 0;
            for (int j = previousLeftBorders.get(i); j <= previousRightBorders.get(i); j++) {
                if (params.squares[j] != 0) {
                    currentSquare = params.squares[j];
                }
                if (params.heights[j] != 0) {
                    currentHeight = params.heights[j];
                }
                if (params.thicknesses[j] != 0) {
                    currentThickness = params.thicknesses[j];
                }
            }
            currentSquare += previousRightBorders.get(i) - previousLeftBorders.get(i) + 1;
            currentHeight++;
            currentWidth = previousRightBorders.get(i) - previousLeftBorders.get(i) + 1;
            if (currentThickness < previousRightBorders.get(i) - previousLeftBorders.get(i) + 1) {
                currentThickness = previousRightBorders.get(i) - previousLeftBorders.get(i) + 1;
            }

            for (int j = previousLeftBorders.get(i); j <= previousRightBorders.get(i); j++) {
                tmpParams.squares[j] = currentSquare;
                tmpParams.heights[j] = currentHeight;
                tmpParams.widths[j] = currentWidth;
                tmpParams.thicknesses[j] = currentThickness;
            }
            params.getCopyOf(tmpParams, previousLeftBorders.get(i), previousRightBorders.get(i));
        }
    }

    private void contourAnalysis(byte[] contour, List<Integer> previousLeftBorders, List<Integer> previousRightBorders,
                                 CornParameters params,
                                 int thresholdSquareMin, int thresholdSquareMax,
                                 int thresholdHeightMin, int thresholdHeightMax,
                                 int thresholdWidthMin, int thresholdWidthMax,
                                 int thresholdThicknessMin, int thresholdThicknessMax) {
        for (int i = 0; i < previousLeftBorders.size(); i++) {
            int sum = 0;
            int currentLeftBorder = previousLeftBorders.get(i);
            int currentRightBorder = previousRightBorders.get(i);
            for (int j = currentLeftBorder; j <= currentRightBorder; j++) {
                sum += contour[j];
            }
            if (sum == 0) {
                boolean squareCondition = params.squares[currentLeftBorder] < thresholdSquareMin || params.squares[currentLeftBorder] > thresholdSquareMax;
                boolean heightCondition = params.heights[currentLeftBorder] < thresholdHeightMin || params.heights[currentLeftBorder] > thresholdHeightMax;
                boolean widthCondition = params.widths[currentLeftBorder] < thresholdWidthMin || params.widths[currentLeftBorder] > thresholdWidthMax;
                boolean thicknessCondition = params.thicknesses[currentLeftBorder] < thresholdThicknessMin || params.thicknesses[currentLeftBorder] > thresholdThicknessMax;
                if (squareCondition || heightCondition || widthCondition || thicknessCondition) {
                    int bullshit = 0;
                }
            }
        }
    }

    private class CornParameters {
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

        void getCopyOf(CornParameters params, int start, int end) {
            getPartArrayCopy(params.squares, this.squares, start, end);
            getPartArrayCopy(params.heights, this.heights, start, end);
            getPartArrayCopy(params.widths, this.widths, start, end);
            getPartArrayCopy(params.thicknesses, this.thicknesses, start, end);
        }

        void getPartArrayCopy(int[] source, int[] dest, int start, int end) {
            for (int i = start; i <= end; i++) {
                dest[i] = source[i];
            }
        }

    }
}
