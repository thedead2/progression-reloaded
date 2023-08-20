package de.thedead2.progression_reloaded.client.gui;

import org.joml.Vector2i;

/**
 * Relative position of an object on a screen.
 * Basically represents a 2x2 Matrix of 2D Vectors, each Vector represents a corner of this element on the screen and holds the x and y coordinates of this corner
 * **/
public class RelativePosition {
    private Vector2i A,     B;
    private Vector2i C,     D;
    private AnchorPoint anchorPoint;
    public RelativePosition(Vector2i vector, int width, int height, AnchorPoint anchorPoint) {
        this(vector.x, vector.y, width, height, anchorPoint);
    }
    public RelativePosition(int x, int y, int width, int height, AnchorPoint anchorPoint) {
        this.init(x, y, width, height, anchorPoint);
    }

    public void init(int x, int y, int width, int height, AnchorPoint anchorPoint){
        this.anchorPoint = anchorPoint;
        Vector2i vector = null;
        switch (this.anchorPoint){
            case A -> vector = new Vector2i(x, y);
            case B -> vector = new Vector2i(x - width, y);
            case C -> vector = new Vector2i(x, y - height);
            case D -> vector = new Vector2i(x - width, y - height);
            case CENTER -> vector = new Vector2i(x - width/2, y - height/2);
        }
        this.A = vector;
        this.B = new Vector2i(A.x + width, A.y);
        this.C = new Vector2i(A.x, A.y + height);
        this.D = new Vector2i(B.x, C.y);
    }

    public int getWidth(){
        return B.x - A.x;
    }
    public int getHeight(){
        return C.y - A.y;
    }
    public boolean isInArea(Vector2i vector){
        return this.isInArea(vector.x, vector.y);
    }
    public boolean isInArea(int x, int y){
        return A.x <= x && x >= B.x && A.y <= y && y >= C.y;
    }

    public Vector2i getAnchorPoint(){
        switch (anchorPoint){
            case A -> {
                return A;
            }
            case B -> {
                return B;
            }
            case C -> {
                return C;
            }
            case D -> {
                return D;
            }
            case CENTER -> {
                return this.getCenter();
            }
            default -> {
                return null;
            }
        }
    }

    public Vector2i getCenter(){
        return new Vector2i(A.x + getWidth()/2, A.y + getHeight()/2);
    }

    public Vector2i getA() {
        return A;
    }

    public Vector2i getB() {
        return B;
    }

    public Vector2i getC() {
        return C;
    }

    public Vector2i getD() {
        return D;
    }

    public int getXMin() {
        return A.x;
    }

    public int getYMin(){
        return A.y;
    }

    public void updateWidth(int width) {
        Vector2i anchor = this.getAnchorPoint();
        this.init(anchor.x, anchor.y, width, this.getHeight(), this.anchorPoint);
    }
    public void updateHeight(int height) {
        Vector2i anchor = this.getAnchorPoint();
        this.init(anchor.x, anchor.y, this.getWidth(), height, this.anchorPoint);
    }

    public void updateAnchorPoint(int x, int y, AnchorPoint anchorPoint){
        this.init(x, y, this.getWidth(), this.getHeight(), anchorPoint);
    }

    public void updateAnchorPoint(AnchorPoint anchorPoint){
        Vector2i vector2i = this.getAnchorPoint();
        this.init(vector2i.x, vector2i.y, this.getWidth(), this.getHeight(), anchorPoint);
    }

    public enum AnchorPoint{
        A,
        B,
        C,
        D,
        CENTER;
    }
}
