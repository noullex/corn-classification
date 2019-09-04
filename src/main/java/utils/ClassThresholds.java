package utils;

public class ClassThresholds {
    private int squareMin;
    private int squareMax;
    private int heightMin;
    private int heightMax;
    private int widthMin;
    private int widthMax;

    public ClassThresholds(){
        squareMin = 0;
        squareMax = 0;
        heightMax = 0;
        heightMin = 0;
        widthMin = 0;
        widthMax = 0;
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
