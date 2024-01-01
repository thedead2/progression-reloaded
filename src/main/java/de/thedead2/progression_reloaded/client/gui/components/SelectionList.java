package de.thedead2.progression_reloaded.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.misc.ScrollBar;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.client.gui.util.Size;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * A renderable representation of a {@link List} that is sortable and searchable.
 */
public class SelectionList<E> extends ScrollableScreenComponent implements List<E> {

    private final List<E> entries = Lists.newArrayList();

    private final List<E> filteredEntries = Lists.newArrayList();

    private final Size entrySize;

    private final int maxEntriesPerRow;

    protected final Supplier<E> hovered = () -> this.getEntryAtPosition(RenderUtil.getMousePos().x, RenderUtil.getMousePos().y);

    private final IEntryRenderer<E> entryRenderer;

    private final int borderColor;

    @Nullable
    protected E selected;

    private IEntrySelectionListener<E> selectionListener;

    private boolean filtered;


    public SelectionList(Area area, Size entrySize, IEntryRenderer<E> entryRenderer) {
        this(area, entrySize, entryRenderer, (content, mouseX, mouseY, button) -> false, 0);
    }


    public SelectionList(Area area, Size entrySize, IEntryRenderer<E> entryRenderer, IEntrySelectionListener<E> selectionListener, int borderColor) {
        super(area, ScrollDirection.VERTICAL, ScrollBar.Visibility.IF_NECESSARY);
        this.entrySize = entrySize;
        this.entryRenderer = entryRenderer;
        this.selectionListener = selectionListener;
        this.borderColor = borderColor;

        if(this.contentArea.getInnerWidth() % this.entrySize.getWidth() != 0) {
            this.entrySize.setWidth(this.contentArea.getInnerWidth() / ((int) (this.contentArea.getInnerWidth() / this.entrySize.getWidth())));
        }

        this.maxEntriesPerRow = (int) (this.contentArea.getInnerWidth() / this.entrySize.getWidth());
    }


    public SelectionList(Area area, Size entrySize, IEntryRenderer<E> entryRenderer, int borderColor) {
        this(area, entrySize, entryRenderer, (content, mouseX, mouseY, button) -> false, borderColor);
    }


    public SelectionList(Area area, Size entrySize, IEntryRenderer<E> entryRenderer, IEntrySelectionListener<E> selectionListener) {
        this(area, entrySize, entryRenderer, selectionListener, 0);
    }


    @Nullable
    public E getSelected() {
        return selected;
    }


    public void setSelected(@Nullable E selected) {
        this.selected = selected;
    }


    public void filter(@Nullable Predicate<E> filter) {
        this.filteredEntries.clear();
        this.filteredEntries.addAll(filter != null ? this.entries.stream().filter(filter).toList() : this.entries);
        this.filtered = filter != null;
    }


    public boolean isFiltered() {
        return this.filtered;
    }


    public void setSelectionListener(IEntrySelectionListener<E> selectionListener) {
        this.selectionListener = selectionListener;
    }


    @Nullable
    public E getHovered() {
        return hovered.get();
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }


    @Override
    protected double scrollRate() {
        return 9.0D / 2.0D;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        else {
            E e = this.getEntryAtPosition(mouseX, mouseY);
            if(e != null) {
                this.setSelected(e);
                return this.selectionListener.mouseClicked(e, mouseX, mouseY, button);
            }
            return false;
        }
    }


    @Nullable
    protected final E getEntryAtPosition(double mouseX, double mouseY) {
        if(this.contentArea.innerContains((float) mouseX, (float) mouseY)) {
            for(int i = 0; i < this.filteredEntries.size(); i++) {
                float xMin = this.getXPosByEntryIndex(i);
                float xMax = xMin + this.entrySize.getWidth();
                float yMin = (float) (this.getYPosByEntryIndex(i) - this.yScrollBar.getScrollAmount());
                float yMax = yMin + this.entrySize.getHeight();

                if(mouseX >= xMin && mouseX <= xMax && mouseY >= yMin && mouseY <= yMax) {
                    return this.filteredEntries.get(i);
                }
            }
        }
        return null;
    }


    protected final float getXPosByEntryIndex(int index) {
        return this.contentArea.getInnerX() + (Mth.clamp(index, 0, this.filteredEntries.size()) % this.maxEntriesPerRow) * this.entrySize.getWidth();
    }


    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    protected final float getYPosByEntryIndex(int index) {
        return this.contentArea.getInnerY() + (Mth.clamp(index, 0, this.filteredEntries.size()) / this.maxEntriesPerRow) * this.entrySize.getHeight();
    }


    @Override
    protected void renderContents(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        for(int i = 0; i < this.filteredEntries.size(); ++i) {
            float xMin = this.getXPosByEntryIndex(i);
            float yMin = this.getYPosByEntryIndex(i);
            float yMax = yMin + this.entrySize.getHeight();
            if(this.withinContentAreaTopBottom(yMin, yMax)) {
                this.renderEntry(poseStack, mouseX, mouseY, partialTick, i, xMin, yMin, this.entrySize.getWidth(), this.entrySize.getHeight());
            }
        }
    }


    protected void renderEntry(PoseStack poseStack, int mouseX, int mouseY, float partialTick, int index, float xMin, float yMin, float width, float height) {
        E entry = this.filteredEntries.get(index);
        if(this.isSelectedItem(index)) {
            int i = this.focused ? -1 : -8355712;
            this.renderSelection(poseStack, xMin, yMin, width, height, i, -16777216);
        }

        this.entryRenderer.render(entry, poseStack, xMin, yMin, this.contentArea.getZ(), width, height, mouseX, mouseY, partialTick);
    }


    protected boolean isSelectedItem(int index) {
        return Objects.equals(this.selected, this.entries.get(index));
    }


    protected void renderSelection(PoseStack poseStack, float xMin, float yMin, float width, float height, int outerColor, int innerColor) {
        //RenderUtil.fill(poseStack, xMin - 1, xMin + width + 1, yMin - 1, yMin + height + 1, this.contentArea.getZ(), outerColor);
        RenderUtil.fill(poseStack, xMin + 1, xMin + width - 1, yMin + 1, yMin + height - 1, this.contentArea.getZ(), innerColor);
    }


    @Override
    protected float contentHeight() {
        return ((float) this.filteredEntries.size() / this.maxEntriesPerRow) * this.entrySize.getHeight();
    }


    @Override
    protected void renderDecorations(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(this.borderColor != 0) {
            RenderUtil.renderArea(poseStack, this.area, RenderUtil.changeAlpha(borderColor, this.alpha), 0);
        }
    }


    @Override
    public int size() {
        return this.entries.size();
    }


    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }


    @Override
    public boolean contains(Object o) {
        return this.entries.contains(o);
    }


    @NotNull
    @Override
    public Iterator<E> iterator() {
        return this.entries.iterator();
    }


    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return this.entries.toArray();
    }


    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return this.entries.toArray(a);
    }


    @Override
    public boolean add(E entry) {
        return this.entries.add(entry);
    }


    @Override
    public boolean remove(Object o) {
        return this.entries.remove(o);
    }


    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.entries.containsAll(c);
    }


    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return this.entries.addAll(c);
    }


    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        return this.entries.addAll(index, c);
    }


    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return this.entries.removeAll(c);
    }


    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return this.entries.retainAll(c);
    }


    @Override
    public void sort(Comparator<? super E> c) {
        this.entries.sort(c);
    }


    @Override
    public void clear() {
        this.entries.clear();
    }


    @Override
    public E get(int index) {
        return this.entries.get(index);
    }


    @Override
    public E set(int index, E element) {
        return this.entries.set(index, element);
    }


    @Override
    public void add(int index, E element) {
        this.entries.add(index, element);
    }


    @Override
    public E remove(int index) {
        return this.entries.remove(index);
    }


    @Override
    public int indexOf(Object o) {
        return this.entries.indexOf(o);
    }


    @Override
    public int lastIndexOf(Object o) {
        return this.entries.lastIndexOf(o);
    }


    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return this.entries.listIterator();
    }


    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return this.entries.listIterator(index);
    }


    @NotNull
    @Override
    public SelectionList<E> subList(int fromIndex, int toIndex) {
        var list = new SelectionList<>(this.area, this.entrySize, this.entryRenderer);
        list.entries.addAll(this.entries.subList(fromIndex, toIndex));

        return list;
    }


    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return this.entries.toArray(generator);
    }


    @FunctionalInterface
    public interface IEntryRenderer<T> {

        void render(T content, PoseStack poseStack, float xMin, float yMin, float zPos, float entryWidth, float entryHeight, int mouseX, int mouseY, float partialTick);
    }

    @FunctionalInterface
    public interface IEntrySelectionListener<T> {

        boolean mouseClicked(T content, double mouseX, double mouseY, int button);
    }
}
