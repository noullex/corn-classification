package utils;

public class ClassThresholds {
    private int squareMin;
    private int squareMax;
    private int heightMin;
    private int heightMax;
    private int widthMin;
    private int widthMax;

    public ClassThresholds() {
    }

    public void init(Corn corn) {
        squareMin = squareMax = corn.getPoints().size();
        widthMin = widthMax = corn.getMaxX() - corn.getMinX();
        heightMin = heightMax = corn.getMaxY() - corn.getMinY();
    }

    public int getSquareMin() {
        return squareMin;
    }

    public void setSquareMin(int squareMin) {
        this.squareMin = squareMin;
    }

    public int getSquareMax() {
        return squareMax;
    }

    public void setSquareMax(int squareMax) {
        this.squareMax = squareMax;
    }

    public int getHeightMin() {
        return heightMin;
    }

    public void setHeightMin(int heightMin) {
        this.heightMin = heightMin;
    }

    public int getHeightMax() {
        return heightMax;
    }

    public void setHeightMax(int heightMax) {
        this.heightMax = heightMax;
    }

    public int getWidthMin() {
        return widthMin;
    }

    public void setWidthMin(int widthMin) {
        this.widthMin = widthMin;
    }

    public int getWidthMax() {
        return widthMax;
    }

    public void setWidthMax(int widthMax) {
        this.widthMax = widthMax;
    }
}
