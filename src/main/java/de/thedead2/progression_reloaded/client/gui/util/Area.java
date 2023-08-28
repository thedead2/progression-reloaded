package de.thedead2.progression_reloaded.client.gui.util;

import com.google.common.base.Objects;
import org.joml.*;

import java.lang.Math;


/**
 * Relative position of an object on a screen.
 * Basically represents a 2x2 Matrix of 2D Vectors, each Vector represents a corner of this element on the screen and holds the x and y coordinates of this corner
 **/
public class Area {

    private Vector2i A, B;

    private Vector2i C, D;

    private Position position;


    public Area(int width, int height, Position position) {
        this.init(width, height, position);
    }


    public Area(Matrix4x3f matrix, Position position) {
        this.position = position;
        this.changeVectorsFromMatrix(matrix);
    }


    private void changeVectorsFromMatrix(Matrix4x3f matrix) {
        Vector3f a = matrix.getColumn(0, new Vector3f());
        Vector3f b = matrix.getColumn(1, new Vector3f());
        Vector3f c = matrix.getColumn(2, new Vector3f());
        Vector3f d = matrix.getColumn(3, new Vector3f());


        this.A = new Vector2i(Math.round(a.x), Math.round(a.y));
        this.B = new Vector2i(Math.round(b.x), Math.round(b.y));
        this.C = new Vector2i(Math.round(c.x), Math.round(c.y));
        this.D = new Vector2i(Math.round(d.x), Math.round(d.y));

        switch(this.position.areaPoint) {
            case A -> this.setPosition(new Position(this.A, this.position.areaPoint));
            case B -> this.setPosition(new Position(this.B, this.position.areaPoint));
            case C -> this.setPosition(new Position(this.C, this.position.areaPoint));
            case D -> this.setPosition(new Position(this.D, this.position.areaPoint));
            case CENTER -> this.setPosition(new Position(this.getCenter(), this.position.areaPoint));
        }
    }


    public Vector2i getCenter() {
        return new Vector2i(A.x + getWidth() / 2, A.y + getHeight() / 2);
    }


    public void init(int width, int height, Position position) {
        this.position = position;
        switch(this.position.areaPoint) {
            case A -> {
                this.A = position.anchor;
                this.B = new Vector2i(A.x + width, A.y);
                this.C = new Vector2i(A.x, A.y + height);
                this.D = new Vector2i(B.x, C.y);
            }
            case B -> {
                this.B = position.anchor;
                this.A = new Vector2i(B.x - width, B.y);
                this.C = new Vector2i(A.x, A.y + height);
                this.D = new Vector2i(B.x, C.y);
            }
            case C -> {
                this.C = position.anchor;
                this.A = new Vector2i(C.x, C.y - height);
                this.B = new Vector2i(A.x + width, A.y);
                this.D = new Vector2i(B.x, C.y);
            }
            case D -> {
                this.D = position.anchor;
                this.A = new Vector2i(D.x - width, D.y - height);
                this.B = new Vector2i(A.x + width, A.y);
                this.C = new Vector2i(A.x, A.y + height);
            }
            case CENTER -> {
                Vector2i anchor = this.position.anchor;
                this.A = new Vector2i(anchor.x - width / 2, anchor.y - height / 2);
                this.B = new Vector2i(A.x + width, A.y);
                this.C = new Vector2i(A.x, A.y + height);
                this.D = new Vector2i(B.x, C.y);
            }
            default -> {
                this.A = new Vector2i();
                this.B = new Vector2i();
                this.C = new Vector2i();
                this.D = new Vector2i();
            }
        }
    }


    public int getWidth() {
        return B.x - A.x;
    }


    public int getHeight() {
        return C.y - A.y;
    }


    public boolean isInArea(Vector2i vector) {
        return this.isInArea(vector.x, vector.y);
    }


    public boolean isInArea(int x, int y) {
        return A.x <= x && x <= B.x && A.y <= y && y <= C.y;
    }


    public void setPosition(int x, int y) {
        this.setPosition(new Position(x, y, this.position.areaPoint));
    }


    public void setAnchorPoint(Point point) {
        this.setPosition(new Position(this.position.anchor, point));
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


    public int getYMin() {
        return A.y;
    }


    public int getXMax() {
        return D.x;
    }


    public int getYMax() {
        return D.y;
    }


    public void rotate(Quaternionf rotation) {
        Matrix4x3f matrix = this.toMatrix().rotate(rotation);
        this.changeVectorsFromMatrix(matrix);
    }


    public Matrix4x3f toMatrix() {
        Vector3f a = new Vector3f(A, 0);
        Vector3f b = new Vector3f(B, 0);
        Vector3f c = new Vector3f(C, 0);
        Vector3f d = new Vector3f(D, 0);

        return new Matrix4x3f(a, b, c, d);
    }


    public void scale(float scale) {
        this.init(Math.round(this.getWidth() * scale), Math.round(this.getHeight() * scale), this.getPosition());
    }


    public Position getPosition() {
        return position;
    }


    public void setPosition(Position position) {
        this.init(this.getWidth(), this.getHeight(), position);
    }


    public Vector2i[] getCorners() {
        Vector2i[] corners = new Vector2i[4];
        corners[0] = this.A;
        corners[1] = this.B;
        corners[2] = this.C;
        corners[3] = this.D;

        return corners;
    }


    public Vector2i[] getSides() {
        Vector2i[] sides = new Vector2i[4];
        sides[0] = this.B.sub(this.A);
        sides[1] = this.C.sub(this.A);
        sides[2] = this.D.sub(this.C);
        sides[3] = this.D.sub(this.B);

        return sides;
    }


    public boolean clashesWith(Area other) {
        return Intersectionf.testAabAab(this.A.x, this.A.y, 0, this.D.x, this.D.y, 0, other.A.x, other.A.y, 0, other.D.x, other.D.y, 0);
    }


    public enum Point {
        A,
        B,
        C,
        D,
        CENTER
    }

    public static class Position {

        private final Vector2i anchor;

        private final Point areaPoint;


        public Position(int x, int y, Point areaPoint) {
            this(new Vector2i(x, y), areaPoint);
        }


        public Position(Vector2i anchor, Point areaPoint) {
            this.anchor = anchor;
            this.areaPoint = areaPoint;
        }


        public Vector2i getAnchor() {
            return anchor;
        }


        public Point getAreaCorner() {
            return areaPoint;
        }


        @Override
        public int hashCode() {
            return Objects.hashCode(anchor, areaPoint);
        }


        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            Position position = (Position) o;
            return Objects.equal(anchor, position.anchor) && areaPoint == position.areaPoint;
        }
    }
}
