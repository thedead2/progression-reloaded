package de.thedead2.progression_reloaded.client.gui.util.objects;

import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.client.gui.util.PoseStackTransformer;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import org.joml.Quaternionf;
import org.joml.Vector2i;

public abstract class RenderObject extends GuiComponent implements Renderable {
    protected Area objectArea;
    protected Area renderArea;
    protected Padding padding;
    protected final int renderLayer;
    protected PoseStackTransformer poseStackTransformer;

    protected RenderObject(int renderLayer, int width, int height, Area.Position position, Padding padding, PoseStackTransformer poseStackTransformer) {
        if(renderLayer < 0 || renderLayer > 12) throw new IllegalStateException("Render layer can only be between 0 and 12!\nCurrent value: " + renderLayer);
        this.renderLayer = renderLayer;
        this.padding = padding;
        this.poseStackTransformer = poseStackTransformer;
        this.init(width, height, position);
    }

    private void init(int width, int height, Area.Position position){
        this.objectArea = new Area(width, height, position);

        int xMinBorder = this.objectArea.getXMin();
        int xMaxBorder = this.objectArea.getXMax();
        int yMinBorder = this.objectArea.getYMin();
        int yMaxBorder = this.objectArea.getYMax();

        int renderAreaXMinBorder = xMinBorder + padding.getLeft();
        int renderAreaXMaxBorder = xMaxBorder - padding.getRight();
        int renderAreaYMinBorder = yMinBorder + padding.getTop();
        int renderAreaYMaxBorder = yMaxBorder - padding.getBottom();

        Vector2i A = new Vector2i(renderAreaXMinBorder, renderAreaYMinBorder);
        Vector2i B = new Vector2i(renderAreaXMaxBorder, renderAreaYMinBorder);
        Vector2i C = new Vector2i(renderAreaXMinBorder, renderAreaYMaxBorder);

        Area.Position renderAreaPosition = new Area.Position(A, Area.Point.A);
        this.renderArea = new Area(B.x - A.x, C.y - A.y, renderAreaPosition);
    }

    public abstract void onResize(int screenWidth, int screenHeight);

    public boolean isInArea(int x, int y){
        return this.objectArea.isInArea(x, y);
    }
    public boolean isInArea(Vector2i vector2i){
        return this.objectArea.isInArea(vector2i);
    }
    public boolean isInRenderArea(int x, int y){
        return this.renderArea.isInArea(x, y);
    }
    public boolean isInRenderArea(Vector2i vector2i){
        return this.renderArea.isInArea(vector2i);
    }

    public int getWidth() {
        return this.objectArea.getWidth();
    }
    public int getHeight() {
        return this.objectArea.getHeight();
    }
    public int getRenderWidth() {
        return this.renderArea.getWidth();
    }
    public int getRenderHeight() {
        return this.renderArea.getHeight();
    }

    public int getRenderLayer() {
        return renderLayer;
    }

    public void setWidth(int width) {
        this.init(width, this.getHeight(), this.getPosition());
    }

    public Area.Position getPosition() {
        return this.objectArea.getPosition();
    }

    public void setHeight(int height) {
        this.init(this.getWidth(), height, this.getPosition());
    }
    public void setPadding(int padding) {
        this.setPadding(padding, padding);
    }
    public void setPadding(int leftRight, int topBottom) {
        this.setPadding(leftRight, leftRight, topBottom, topBottom);
    }
    public void setPadding(int left, int right, int top, int bottom) {
        this.setPadding(new Padding(left, right, top, bottom));
    }
    public void setPadding(Padding padding) {
        this.padding = padding;
        this.init(this.getWidth(), this.getHeight(), this.getPosition());
    }

    public void setPosition(Vector2i position, Area.Point anchorPoint) {
        this.setPosition(new Area.Position(position, anchorPoint));
    }

    public void setPosition(Area.Position position) {
        this.init(this.getWidth(), this.getHeight(), position);
    }

    public Vector2i getCenter() {
        return this.objectArea.getCenter();
    }

    public void rotate(Quaternionf rotation) {
        this.objectArea.rotate(rotation);
        this.renderArea.rotate(rotation);
    }

    public static abstract class Builder<T> {

        public abstract T build();
    }
}
