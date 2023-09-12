package de.thedead2.progression_reloaded.client.gui.util.objects;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;


public abstract class RenderObject extends GuiComponent implements Renderable {

    protected final Area renderArea;

    protected final Vector3f scale;

    protected Padding padding;

    protected final Quaternionf xRot;

    protected final Quaternionf yRot;

    protected final Quaternionf zRot;

    private final Area objectArea;

    protected Area.Position position;


    protected RenderObject(float xPos, float yPos, float zPos, Area.AnchorPoint anchorPoint, float width, float height, Padding padding) {
        this(xPos, yPos, zPos, anchorPoint, width, height, new Quaternionf(), new Quaternionf(), new Quaternionf(), padding);
    }


    protected RenderObject(float xPos, float yPos, float zPos, Area.AnchorPoint anchorPoint, float width, float height, Quaternionf xRot, Quaternionf yRot, Quaternionf zRot, Padding padding) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
        this.padding = padding;
        this.scale = new Vector3f(1, 1, 1);
        this.position = new Area.Position(xPos, yPos, zPos, anchorPoint);
        this.objectArea = new Area(0, 0, zPos, anchorPoint, width, height);
        this.renderArea = new Area(this.objectArea.getAx() + this.padding.getLeft(), this.objectArea.getAy() + this.padding.getTop(), this.objectArea.getAz(), Area.AnchorPoint.A, width - (this.padding.getLeft() + this.padding.getRight()), height - (this.padding.getTop() + this.padding.getBottom()));
    }


    public boolean isInArea(float x, float y) {
        PoseStack poseStack = new PoseStack();
        this.getPoseStackTransformation(poseStack);
        Matrix4f matrix4f = poseStack.last().pose();
        Vector4f vector4f = matrix4f.transform(new Vector4f(x, y, 1, 1));
        return this.objectArea.isInArea(vector4f.x, vector4f.y);
    }


    public void getPoseStackTransformation(PoseStack poseStack) {
        Vector3f pos = this.position.getAnchor();
        poseStack.translate(pos.x, pos.y, pos.z);
        poseStack.scale(this.scale.x, this.scale.y, this.scale.z);
        poseStack.mulPose(this.xRot);
        poseStack.mulPose(this.yRot);
        poseStack.mulPose(this.zRot);
    }


    public boolean isInRenderArea(float x, float y) {
        PoseStack poseStack = new PoseStack();
        this.getPoseStackTransformation(poseStack);
        Matrix4f matrix4f = poseStack.last().pose();
        Vector4f vector4f = matrix4f.transform(new Vector4f(x, y, 1, 1));
        return this.renderArea.isInArea(vector4f.x, vector4f.y);
    }


    public float getRenderWidth() {
        return this.renderArea.getWidth();
    }


    public float getRenderHeight() {
        return this.renderArea.getHeight();
    }


    public void setPadding(float padding) {
        this.setPadding(padding, padding);
    }


    public void setPadding(float leftRight, float topBottom) {
        this.setPadding(leftRight, leftRight, topBottom, topBottom);
    }


    public void setPadding(float left, float right, float top, float bottom) {
        this.setPadding(new Padding(left, right, top, bottom));
    }


    public void setPadding(Padding padding) {
        this.padding = padding;
        this.updateRenderArea();
    }


    //Approved
    private void updateRenderArea() {
        this.renderArea.setWidth(this.objectArea.getWidth() - (this.padding.getLeft() + this.padding.getRight()));
        this.renderArea.setHeight(this.objectArea.getHeight() - (this.padding.getTop() + this.padding.getBottom()));
        this.renderArea.changePosition(new Area.Position(this.objectArea.getAx() + this.padding.getLeft(), this.objectArea.getAy() + this.padding.getTop(), this.objectArea.getAz(), Area.AnchorPoint.A));
    }


    //Approved
    public float getWidth() {
        return this.objectArea.getWidth();
    }


    //Approved
    public void setWidth(float width) {
        this.objectArea.setWidth(width);
        this.updateRenderArea();
    }


    //Approved
    public float getHeight() {
        return this.objectArea.getHeight();
    }


    //Approved
    public void setHeight(float height) {
        this.objectArea.setHeight(height);
        this.updateRenderArea();
    }


    public Vector3f getCenter() {
        return this.objectArea.getCenter();
    }


    //TODO: Rotation and Scaling isn't working!
    public void rotate(Quaternionf xRot, Quaternionf yRot, Quaternionf zRot) {
        this.rotateX(xRot);
        this.rotateY(yRot);
        this.rotateZ(zRot);
    }


    public void rotateX(Quaternionf xRot) {
        this.xRot.set(xRot);
    }


    public void rotateY(Quaternionf yRot) {
        this.yRot.set(yRot);
    }


    public void rotateZ(Quaternionf zRot) {
        this.zRot.set(zRot);
    }


    public void scale(float x, float y) {
        this.scale.set(x, y, 1);
    }


    public Area getRenderArea() {
        return renderArea;
    }


    public float getXMax() {
        return this.objectArea.getXMax();
    }


    public float getXMin() {
        return this.objectArea.getXMin();
    }


    public float getYMax() {
        return this.objectArea.getYMax();
    }


    public float getYMin() {
        return this.objectArea.getYMin();
    }


    public float getZMax() {
        return this.objectArea.getZMax();
    }


    public float getZMin() {
        return this.objectArea.getZMin();
    }


    public void changePosition(Area.Position position) {
        this.position = position;
        this.objectArea.changePosition(new Area.Position(0, 0, this.position.getAnchor().z, this.position.getAnchorPoint()));
        this.updateRenderArea();
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();
        this.getPoseStackTransformation(poseStack);
        this.renderInternal(poseStack, mouseX, mouseY, partialTick);
        poseStack.popPose();
    }


    protected abstract void renderInternal(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick);


    public Area getObjectArea() {
        return this.objectArea;
    }


    public Vector3f getPositionAnchor() {
        return this.position.getAnchor();
    }


    public Area.AnchorPoint getAnchorPoint() {
        return this.position.getAnchorPoint();
    }


    public static abstract class Builder<T> {

        public abstract T build();
    }

}
