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


    public float getXPos(float origin, float screenWidth, float width, float offset) {
        return x.getXPos(origin, screenWidth, width) + offset;
    }


    public float getYPos(float origin, float screenHeight, float height, float offset) {
        return y.getYPos(origin, screenHeight, height) + offset;
    }


    public enum X {
        LEFT {
            @Override
            public float getXPos(float origin, float screenWidth, float width) {
                return origin;
            }
        },
        RIGHT {
            @Override
            public float getXPos(float origin, float screenWidth, float width) {
                return origin + (screenWidth - width);
            }
        },
        CENTER {
            @Override
            public float getXPos(float origin, float screenWidth, float width) {
                return origin + ((screenWidth - width) / 2);
            }
        };


        protected abstract float getXPos(float origin, float screenWidth, float width);
    }

    public enum Y {
        TOP {
            @Override
            public float getYPos(float origin, float screenHeight, float height) {
                return origin;
            }
        },
        BOTTOM {
            @Override
            public float getYPos(float origin, float screenHeight, float height) {
                return origin + (screenHeight - height);
            }
        },
        CENTER {
            @Override
            public float getYPos(float origin, float screenHeight, float height) {
                return origin + ((screenHeight - height) / 2);
            }
        };


        protected abstract float getYPos(float origin, float screenHeight, float height);
    }
}
