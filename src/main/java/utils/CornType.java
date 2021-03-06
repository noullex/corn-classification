package utils;

public enum CornType {
    UNKNOWN(0),
    BARLEY(1),
    BUCKWHEAT(2),
    RICE(3);

    int value;

    CornType(int val) {
        value = val;
    }

    public int getValue() {
        return value;
    }
}