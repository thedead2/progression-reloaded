package de.thedead2.progression_reloaded.client.gui.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public record Alignment(XAlignment xAlignment, YAlignment yAlignment) {

    public static Alignment DEFAULT = new Alignment(XAlignment.LEFT, YAlignment.TOP);

    public static Alignment CENTERED = new Alignment(XAlignment.CENTER, YAlignment.CENTER);

    public static Alignment LEFT_CENTERED = new Alignment(XAlignment.LEFT, YAlignment.CENTER);

    public static Alignment RIGHT_CENTERED = new Alignment(XAlignment.RIGHT, YAlignment.CENTER);

    public static Alignment TOP_CENTERED = new Alignment(XAlignment.CENTER, YAlignment.TOP);

    public static Alignment BOTTOM_CENTERED = new Alignment(XAlignment.CENTER, YAlignment.BOTTOM);


    public static Alignment fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        XAlignment xAlignment = XAlignment.valueOf(jsonObject.get("x").getAsString());
        YAlignment yAlignment = YAlignment.valueOf(jsonObject.get("y").getAsString());

        return new Alignment(xAlignment, yAlignment);
    }


    public float getXPos(float origin, float screenWidth, float width, float offset) {
        return this.xAlignment.getXPos(origin, screenWidth, width) + offset;
    }


    public float getYPos(float origin, float screenHeight, float height, float offset) {
        return this.yAlignment.getYPos(origin, screenHeight, height) + offset;
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("x", this.xAlignment.name());
        jsonObject.addProperty("y", this.yAlignment.name());

        return jsonObject;
    }


    public enum XAlignment {
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


        public abstract float getXPos(float origin, float screenWidth, float width);
    }

    public enum YAlignment {
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


        public abstract float getYPos(float origin, float screenHeight, float height);
    }
}
