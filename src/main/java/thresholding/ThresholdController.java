package thresholding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ClassThresholds;
import utils.CornInterval;
import utils.Utils;
import utils.imagePreprocessing.Binarizer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThresholdController {
    private static final Logger log = LoggerFactory.getLogger(ThresholdController.class);

    private int width;
    private int height;

    public void separate(ClassThresholds classThresholds, BufferedImage image, BufferedImage background) throws IOException {
        List<Point> defectiveCorns = getDefectiveCorns(classThresholds, image, background);
        Utils.drawDefectiveCorns(image, defectiveCorns);
        log.info(Utils.getSeparationStats(defectiveCorns));
    }

    private List<Point> getDefectiveCorns(ClassThresholds classThresholds, BufferedImage image, BufferedImage background) {
        width = image.getWidth();
        height = image.getHeight();
        byte[][] binaryImage = Binarizer.binarize(Binarizer.subtract(image, background));
        List<Point> defectiveCorns = new ArrayList<>();
        CornParameters params = new CornParameters(width);
        List<CornInterval> previousCornIntervals = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            List<CornInterval> currentCornIntervals = new ArrayList<>();
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

            for (int i = 0; i < currentLeftBorders.size(); i++) {
                currentCornIntervals.add(new CornInterval(currentLeftBorders.get(i), currentRightBorders.get(i)));
            }

            params = calculateCornParams(previousCornIntervals, params);
            List<List<Integer>> defectiveCornsParams = contourAnalysis(binaryImage[y], previousCornIntervals, params,
                    classThresholds.getSquareMin(), classThresholds.getSquareMax(),
                    classThresholds.getHeightMin(), classThresholds.getHeightMax(),
                    classThresholds.getWidthMin(), classThresholds.getWidthMax());
            for (List<Integer> defectiveCornParams : defectiveCornsParams) {
                int centerX = defectiveCornParams.get(0);
                int centerY = ((y - defectiveCornParams.get(1)) + y) / 2;
                Point cornCenter = new Point(centerX, centerY);
                defectiveCorns.add(cornCenter);
            }
            previousCornIntervals = currentCornIntervals;
        }
        return defectiveCorns;
    }

    private CornParameters calculateCornParams(List<CornInterval> currentCornIntervals, CornParameters params) {
        CornParameters tmpParams = new CornParameters(width);
        for (CornInterval currentCornInterval : currentCornIntervals) {
            int currentRightBorder = currentCornInterval.getRightBorder();
            int currentLeftBorder = currentCornInterval.getLeftBorder();
            int currentSquare = 0;
            int currentHeight = 0;
            int currentWidth = 0;
            for (int j = currentLeftBorder; j <= currentRightBorder; j++) {
                if (params.squares[j] != 0) {
                    currentSquare = params.squares[j];
                }
                if (params.heights[j] != 0) {
                    currentHeight = params.heights[j];
                }
                if (params.width[j] != 0) {
                    currentWidth = params.width[j];
                }
            }
            currentSquare += currentRightBorder - currentLeftBorder + 1;
            currentHeight++;
            if (currentWidth < currentRightBorder - currentLeftBorder + 1) {
                currentWidth = currentRightBorder - currentLeftBorder + 1;
            }

            for (int j = currentLeftBorder; j <= currentRightBorder; j++) {
                tmpParams.squares[j] = currentSquare;
                tmpParams.heights[j] = currentHeight;
                tmpParams.width[j] = currentWidth;
            }
        }
        return tmpParams;
    }

    private List<List<Integer>> contourAnalysis(byte[] contour, List<CornInterval> previousCornIntervals,
                                                CornParameters params,
                                                int thresholdSquareMin, int thresholdSquareMax,
                                                int thresholdHeightMin, int thresholdHeightMax,
                                                int thresholdWidthMin, int thresholdWidthMax) {
        List<List<Integer>> defectiveCorns = new ArrayList<>();
        for (CornInterval previousCornInterval : previousCornIntervals) {
            int sum = 0;
            int currentLeftBorder = previousCornInterval.getLeftBorder();
            int currentRightBorder = previousCornInterval.getRightBorder();
            for (int j = currentLeftBorder; j <= currentRightBorder; j++) {
                sum += contour[j];
            }
            if (sum == 0) {
                boolean squareCondition = params.squares[currentLeftBorder] < thresholdSquareMin || params.squares[currentLeftBorder] > thresholdSquareMax;
                boolean heightCondition = params.heights[currentLeftBorder] < thresholdHeightMin || params.heights[currentLeftBorder] > thresholdHeightMax;
                boolean widthCondition = params.width[currentLeftBorder] < thresholdWidthMin || params.width[currentLeftBorder] > thresholdWidthMax;
                if (squareCondition || heightCondition || widthCondition) {
                    int centerX = (previousCornInterval.getRightBorder() + previousCornInterval.getLeftBorder()) / 2;
                    int shiftByY = params.heights[currentLeftBorder];
                    defectiveCorns.add(Arrays.asList(centerX, shiftByY));
                }
            }
        }
        return defectiveCorns;
    }
}
