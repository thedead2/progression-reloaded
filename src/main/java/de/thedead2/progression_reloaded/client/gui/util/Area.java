package de.thedead2.progression_reloaded.client.gui.util;

import com.google.common.base.Objects;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import org.joml.Intersectionf;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;


/**
 * Relative position of an object on a screen.
 * Represents a 4x3 Matrix of coordinates.
 * <p>
 * x    y    z
 * A m00, m01, m02;
 * B m10, m11, m12;
 * C m20, m21, m22;
 * D m30, m31, m32;
 * </p>
 * A    B
 * C    D
 **/

public class Area {

    private final Matrix4x3f vectorMatrix;

    private Position position;


    public Area(float xPos, float yPos, AnchorPoint anchorPoint, float width, float height) {
        this(xPos, yPos, 0, anchorPoint, width, height);
    }


    public Area(float xPos, float yPos, float zPos, AnchorPoint anchorPoint, float width, float height) {
        this.vectorMatrix = new Matrix4x3f();
        this.position = new Position(xPos, yPos, zPos, anchorPoint);
        this.updatePosition(width, height);
    }


    public void updatePosition(float width, float height) {
        final Vector3f A, B, C, D;
        float zPos = this.position.anchor.z;
        switch(this.position.anchorPoint) {
            case B -> {
                B = this.position.anchor;
                A = new Vector3f(B.x - width, B.y, zPos);
                C = new Vector3f(A.x, A.y + height, zPos);
                D = new Vector3f(B.x, C.y, zPos);
            }
            case C -> {
                C = this.position.anchor;
                A = new Vector3f(C.x, C.y - height, zPos);
                B = new Vector3f(A.x + width, A.y, zPos);
                D = new Vector3f(B.x, C.y, zPos);
            }
            case D -> {
                D = this.position.anchor;
                A = new Vector3f(D.x - width, D.y - height, zPos);
                B = new Vector3f(A.x + width, A.y, zPos);
                C = new Vector3f(A.x, A.y + height, zPos);
            }
            case CENTER -> {
                Vector3f anchor = this.position.anchor;
                A = new Vector3f(anchor.x - width / 2, anchor.y - height / 2, zPos);
                B = new Vector3f(A.x + width, A.y, zPos);
                C = new Vector3f(A.x, A.y + height, zPos);
                D = new Vector3f(B.x, C.y, zPos);
            }
            default -> {
                A = this.position.anchor;
                B = new Vector3f(A.x + width, A.y, zPos);
                C = new Vector3f(A.x, A.y + height, zPos);
                D = new Vector3f(A.x + width, A.y + height, zPos);
            }
        }

        this.vectorMatrix.set(A, B, C, D);
    }


    public void changePosition(Position position) {
        this.position = position;
        this.updatePosition(this.getWidth(), this.getHeight());
    }


    public float getWidth() {
        return getAB().length();
    }


    public float getHeight() {
        return getAC().length();
    }


    public Vector3f getAB() {
        return getB().sub(getA());
    }


    public Vector3f getAC() {
        return getC().sub(getA());
    }


    public Vector3f getB() {
        return this.vectorMatrix.getColumn(1, new Vector3f());
    }


    public Vector3f getA() {
        return this.vectorMatrix.getColumn(0, new Vector3f());
    }


    public Vector3f getC() {
        return this.vectorMatrix.getColumn(2, new Vector3f());
    }


    public void setC(Vector3f c) {
        this.vectorMatrix.setColumn(2, c);
    }


    public void setA(Vector3f a) {
        this.vectorMatrix.setColumn(0, a);
    }


    public void setB(Vector3f b) {
        this.vectorMatrix.setColumn(1, b);
    }


    public void setHeight(float height) {
        Vector3f A = this.getA();
        Vector3f oldC = this.getC();
        Vector3f oldD = this.getD();
        Vector3f newC = new Vector3f(oldC.x, A.y + height, oldC.z);
        Vector3f newD = new Vector3f(oldD.x, A.y + height, oldD.z);

        this.setC(newC);
        this.setD(newD);
    }


    public void setWidth(float width) {
        Vector3f A = this.getA();
        Vector3f oldB = this.getB();
        Vector3f oldD = this.getD();
        Vector3f newB = new Vector3f(A.x + width, oldB.y, oldB.z);
        Vector3f newD = new Vector3f(newB.x, oldD.y, oldD.z);

        this.setB(newB);
        this.setD(newD);
    }


    public Vector3f getCenter() {
        return MathHelper.getIntersectionPoint(getA(), getAD(), getB(), getBC());
    }


    public Vector3f getAD() {
        return getD().sub(getA());
    }


    public Vector3f getBC() {
        return getC().sub(getB());
    }


    public Vector3f getD() {
        return this.vectorMatrix.getColumn(3, new Vector3f());
    }


    public void setD(Vector3f d) {
        this.vectorMatrix.setColumn(3, d);
    }


    public boolean isInArea(float x, float y) {
        return getXMin() <= x && x <= getXMax() && getYMin() <= y && y <= getYMax();
    }


    public float getXMin() {
        float f = Math.min(this.vectorMatrix.m00(), this.vectorMatrix.m10());
        f = Math.min(f, this.vectorMatrix.m20());
        f = Math.min(f, this.vectorMatrix.m30());
        return f;
    }


    public float getXMax() {
        float f = Math.max(this.vectorMatrix.m00(), this.vectorMatrix.m10());
        f = Math.max(f, this.vectorMatrix.m20());
        f = Math.max(f, this.vectorMatrix.m30());
        return f;
    }


    public float getYMin() {
        float f = Math.min(this.vectorMatrix.m01(), this.vectorMatrix.m11());
        f = Math.min(f, this.vectorMatrix.m21());
        f = Math.min(f, this.vectorMatrix.m31());
        return f;
    }


    public float getYMax() {
        float f = Math.max(this.vectorMatrix.m01(), this.vectorMatrix.m11());
        f = Math.max(f, this.vectorMatrix.m21());
        f = Math.max(f, this.vectorMatrix.m31());
        return f;
    }


    public Vector3f getBD() {
        return getD().sub(getB());
    }


    public Vector3f getCD() {
        return getD().sub(getC());
    }


    public float getZMin() {
        float f = Math.min(this.vectorMatrix.m02(), this.vectorMatrix.m12());
        f = Math.min(f, this.vectorMatrix.m22());
        f = Math.min(f, this.vectorMatrix.m32());
        return f;
    }


    public float getZMax() {
        float f = Math.max(this.vectorMatrix.m02(), this.vectorMatrix.m12());
        f = Math.max(f, this.vectorMatrix.m22());
        f = Math.max(f, this.vectorMatrix.m32());
        return f;
    }


    public boolean clashesWith(Area other) {
        return Intersectionf.testAabAab(this.getA(), other.getA(), this.getD(), other.getD());
    }


    public void set(Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
        this.vectorMatrix.set(a, b, c, d);
    }


    public float getAx() {
        return this.vectorMatrix.m00();
    }


    public float getAy() {
        return this.vectorMatrix.m01();
    }


    public float getAz() {
        return this.vectorMatrix.m02();
    }


    public float getBx() {
        return this.vectorMatrix.m10();
    }


    public float getBy() {
        return this.vectorMatrix.m11();
    }


    public float getBz() {
        return this.vectorMatrix.m12();
    }


    public float getCx() {
        return this.vectorMatrix.m20();
    }


    public float getCy() {
        return this.vectorMatrix.m21();
    }


    public float getCz() {
        return this.vectorMatrix.m22();
    }


    public float getDx() {
        return this.vectorMatrix.m30();
    }


    public float getDy() {
        return this.vectorMatrix.m31();
    }


    public float getDz() {
        return this.vectorMatrix.m32();
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(vectorMatrix, position);
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        Area area = (Area) o;
        return Objects.equal(vectorMatrix, area.vectorMatrix) && Objects.equal(position, area.position);
    }


    public enum AnchorPoint {
        A,
        B,
        C,
        D,
        CENTER
    }

    public static class Position {

        private final Vector3f anchor;

        private final AnchorPoint anchorPoint;


        public Position(float x, float y, float z, AnchorPoint anchorPoint) {
            this(new Vector3f(x, y, z), anchorPoint);
        }


        public Position(Vector3f anchor, AnchorPoint anchorPoint) {
            this.anchor = anchor;
            this.anchorPoint = anchorPoint;
        }


        public Vector3f getAnchor() {
            return anchor;
        }


        public AnchorPoint getAnchorPoint() {
            return anchorPoint;
        }


        @Override
        public int hashCode() {
            return Objects.hashCode(anchor, anchorPoint);
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
            return Objects.equal(anchor, position.anchor) && anchorPoint == position.anchorPoint;
        }
    }
}
