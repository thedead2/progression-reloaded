package de.thedead2.progression_reloaded.client.gui.util;

public class Alignment {

    public static Alignment DEFAULT = new Alignment(X.LEFT, Y.TOP);

    public static Alignment CENTERED = new Alignment(X.CENTER, Y.CENTER);

    public static Alignment LEFT_CENTERED = new Alignment(X.LEFT, Y.CENTER);

    public static Alignment RIGHT_CENTERED = new Alignment(X.RIGHT, Y.CENTER);

    public static Alignment TOP_CENTERED = new Alignment(X.CENTER, Y.TOP);

    public static Alignment BOTTOM_CENTERED = new Alignment(X.CENTER, Y.BOTTOM);

    private final X x;

    private final Y y;


    public Alignment(X x, Y y) {
        this.x = x;
        this.y = y;
    }


    public float getXPos(float screenWidth, float width) {
        return x.getXPos(screenWidth, width);
    }


    public float getYPos(float screenHeight, float height) {
        return y.getYPos(screenHeight, height);
    }


    public enum X {
        LEFT {
            @Override
            public float getXPos(float screenWidth, float width) {
                return 0;
            }
        },
        RIGHT {
            @Override
            public float getXPos(float screenWidth, float width) {
                return screenWidth - width;
            }
        },
        CENTER {
            @Override
            public float getXPos(float screenWidth, float width) {
                return (screenWidth - width) / 2;
            }
        };


        protected abstract float getXPos(float screenWidth, float width);
    }

    public enum Y {
        TOP {
            @Override
            public float getYPos(float screenHeight, float height) {
                return 0;
            }
        },
        BOTTOM {
            @Override
            public float getYPos(float screenHeight, float height) {
                return screenHeight - height;
            }
        },
        CENTER {
            @Override
            public float getYPos(float screenHeight, float height) {
                return (screenHeight - height) / 2;
            }
        };


        protected abstract float getYPos(float screenHeight, float height);
    }
}
