package de.thedead2.progression_reloaded.gui.editors;

import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.api.criteria.ITab;
import de.thedead2.progression_reloaded.api.event.TabVisibleEvent;
import de.thedead2.progression_reloaded.gui.buttons.ButtonNewCriteria;
import de.thedead2.progression_reloaded.gui.buttons.ButtonTab;
import de.thedead2.progression_reloaded.gui.core.GuiCore;
import de.thedead2.progression_reloaded.gui.core.GuiList;
import de.thedead2.progression_reloaded.gui.editors.TreeEditorElement.ColorMode;
import de.thedead2.progression_reloaded.handlers.APICache;
import de.thedead2.progression_reloaded.handlers.RuleHandler;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import de.thedead2.progression_reloaded.json.Options;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static de.thedead2.progression_reloaded.api.special.DisplayMode.DISPLAY;
import static de.thedead2.progression_reloaded.api.special.DisplayMode.EDIT;
import static de.thedead2.progression_reloaded.gui.core.GuiList.*;

public class GuiTreeEditor extends GuiBaseEditor implements IEditorMode {
    private static final HashMap<ITab, Boolean> tabCache = new HashMap();
    private HashMap<ICriteria, TreeEditorElement> elements;
    public ICriteria selected = null;
    public ICriteria previous = null;
    public UUID currentTabID;
    public ITab currentTab;
    public int ticksExisted;

    public GuiTreeEditor() {
        features.add(BACKGROUND);
        features.add(TREE_ELEMENT);
        features.add(TEMPLATE_SELECTOR_TAB);
        features.add(TEMPLATE_SELECTOR_CRITERIA);
    }

    @Override
    public Object getKey() {
        return currentTab;
    }

    @Override
    public boolean hasButtons() {
        return true;
    }

    public void addButtons(GuiCore core, boolean sideWays) {
        List<GuiButton> buttonList = core.getButtonNewList(); //Recreate the button list, in order to reposition it
        int position = 0;
        int posY = -5;
        if (MODE == EDIT) {
            if (!sideWays) {
                buttonList.add(new ButtonNewCriteria(0, posY));
                posY += 28;
            } else buttonList.add(new ButtonNewCriteria(posY + 10, -22).setSideways());
            position++;
        }

        //Sort tabs alphabetically or by sort index
        for (ITab tab : APICache.getCache(true).getSortedTabs()) {
            if (isTabVisible(tab) || MODE == EDIT) {
                if (!sideWays) {
                    if (position <= 8) {
                        buttonList.add(new ButtonTab(tab, 0, posY));
                    } else buttonList.add(new ButtonTab(tab, core.res.getScaledWidth() - 25, posY));

                    posY += 28;
                    if (position == 8) {
                        posY = -5;
                    }

                    position++;
                } else {
                    boolean isEven = (position - 1) % 2 == 0;
                    if (!isEven) {
                        buttonList.add(new ButtonTab(tab, posY + 10, -22).setSideways());
                    } else {
                        buttonList.add(new ButtonTab(tab, posY + 10, core.ySize).setBottom().setSideways());
                        posY += 28;
                    }

                    position++;
                }
            }
        }
    }

    public void onClientSetup() {
        //Attempt to grab from the config
        if (currentTabID == null) {
            currentTabID = Options.getSettings().defaultTabID;
        }

        currentTab = APICache.getClientCache().getTab(currentTabID); //Attempt to grab the tab
        //If the config fails, grab the first instance
        if (currentTab == null) {
            APICache.getClientCache().clearSorted();
            for (ITab tab : APICache.getClientCache().getSortedTabs()) {
                currentTabID = tab.getUniqueID();
                break;
            }
        }

        currentTab = APICache.getClientCache().getTab(currentTabID); //Attempt to grab the tab
        if (currentTab == null) { //If the tab was null, create a new blank one
            currentTab = RuleHandler.newTab(UUID.randomUUID(), true).setDisplayName("New Tab").setStack(new ItemStack(Items.BOOK)).setVisibility(true);
            currentTabID = currentTab.getUniqueID();
        }
    }

    @Override
    public void initData() {
        addButtons(CORE, APICache.getClientCache().getSortedTabs().size() > 17);
        currentTab = APICache.getClientCache().getTab(currentTabID);
        if (currentTab == null) {
            MCClientHelper.getPlayer().closeScreen();
        } else {
            tabCache.clear();
            rebuildCriteria();
        }
    }

    public void rebuildCriteria() {
        //Rebuild
        elements = new HashMap();
        for (ICriteria criteria : currentTab.getCriteria()) {
            elements.put(criteria, new TreeEditorElement(criteria));
        }
    }

    public TreeEditorElement getElement(ICriteria criteria) {
        return elements.get(criteria);
    }

    public void addCriteria(ICriteria criteria, int x, int y, int offsetX) {
        elements.put(criteria, new TreeEditorElement(criteria));
        TreeEditorElement element = getElement(criteria);
        if (element == null) {
            element = new TreeEditorElement(criteria);
            elements.put(criteria, element);
        }

        element.draw(x, y, offsetX);
        element.click(x, y, false);
        lastClicked = criteria;
    }

    public static boolean isTabVisible(ITab tab) { //Use the cached value, which we update everytime this gui is reopened
        if (tabCache.containsKey(tab)) return tabCache.get(tab);
        boolean result = tab.isVisible();
        TabVisibleEvent event = new TabVisibleEvent(MCClientHelper.getPlayer(), tab.getUniqueID());
        if (MinecraftForge.EVENT_BUS.post(event)) result = false;
        tabCache.put(tab, result);
        return result;
    }
    
    @Override
    public IEditorMode getPreviousGui() {
        return this;
    }

    @Override
    public void drawGuiForeground(boolean overlayvisible, int mouseX, int mouseY) {
        if (MODE == DISPLAY && !isTabVisible(currentTab)) return;
        for (ICriteria criteria : currentTab.getCriteria()) {
            TreeEditorElement element = getElement(criteria);
            if (element != null) {
                if (MODE == DISPLAY) {
                    ColorMode mode = TreeEditorElement.getModeForCriteria(criteria, false);
                    if (mode == ColorMode.INVISIBLE) continue;
                }

                if (element.isCriteriaVisible() || MODE == EDIT) {
                    List<ICriteria> prereqs = criteria.getPreReqs();
                    for (ICriteria c : prereqs) {
                        int y1 = getElement(c).getY();
                        int y2 = element.getY();
                        int x1 = getElement(c).getX();
                        int x2 = element.getX();

                        int width;
                        int textWidth = CORE.mc.fontRendererObj.getStringWidth(c.getLocalisedName());
                        int iconWidth = 9 + (c.getRewards().size() * 12);
                        if (textWidth >= iconWidth) {
                            width = textWidth + 9;
                        } else width = iconWidth;

                        width -= 3;

                        if (c.getTab() == criteria.getTab()) {
                            drawLine(CORE.getOffsetX() + width + x1, 12 + y1 - 1, CORE.getOffsetX() + 5 + x2, 12 + y2 - 1, 1, GuiList.THEME.connectLineColor1);
                            drawLine(CORE.getOffsetX() + width + x1, 12 + y1 + 1, CORE.getOffsetX() + 5 + x2, 12 + y2 + 1, 1, GuiList.THEME.connectLineColor2); //#636C69
                            drawLine(CORE.getOffsetX() + width + x1, 12 + y1, CORE.getOffsetX() + 5 + x2, 12 + y2, 1, GuiList.THEME.connectLineColor3);
                        }
                    }
                }
            }
        }

        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        for (ICriteria criteria : currentTab.getCriteria()) {
            TreeEditorElement element = getElement(criteria);
            if (element != null) {
                if (element.isCriteriaVisible() || MODE == EDIT) {
                    element.draw(0, CORE.screenTop, CORE.getOffsetX());
                }
            }
        }
    }

    public ITab previousTab = null;

    @Override
    public void keyTyped(char character, int key) {
        ICriteria toRemove = null;
        for (ICriteria criteria : currentTab.getCriteria()) {
            TreeEditorElement element = getElement(criteria);
            if (element != null) {
                if (element.isCriteriaVisible() || MODE == EDIT) {
                    if (element.keyTyped(character, key)) {
                        toRemove = criteria;
                        break;
                    }
                }
            }
        }

        if (toRemove != null) {
            RuleHandler.removeCriteria(toRemove.getUniqueID(), false);
        }

        if (key == Keyboard.KEY_UP) {
            currentTab.setSortIndex(currentTab.getSortIndex() + 1);
            APICache.getClientCache().clearSorted(); //Clear the sorted
            CORE.initGui();
        } else if (key == Keyboard.KEY_DOWN) {
            currentTab.setSortIndex(currentTab.getSortIndex() - 1);
            APICache.getClientCache().clearSorted(); //Clear the sorted
            CORE.initGui();
        }
    }

    @Override
    public void guiMouseReleased(int mouseX, int mouseY, int button) {
        for (ICriteria criteria : currentTab.getCriteria()) {
            TreeEditorElement element = getElement(criteria);
            if (element != null) {
                element.release(mouseX, mouseY);
            }
        }

        isDragging = false;
    }

    public long lastClick;
    public int lastType;
    public ICriteria lastClicked = null;
    public int drag = 0;
    public boolean isDragging = false;
    public int lastX;
    public int lastY;

    @Override
    public boolean guiMouseClicked(boolean overlayvisible, int mouseX, int mouseY, int button) {
        if (currentTab == null) return false;
        long thisClick = System.currentTimeMillis();
        long difference = thisClick - lastClick;
        boolean isDoubleClick = button == 0 && lastType == 0 && difference <= 500 && lastX == mouseX && lastY == mouseY;
        lastClick = System.currentTimeMillis();
        lastType = button;
        lastX = mouseX;
        lastY = mouseY;
        lastClicked = null;

        if (button == 0) {
            for (ICriteria criteria : currentTab.getCriteria()) {
                TreeEditorElement element = getElement(criteria);
                if (element != null) {
                    if (MODE == DISPLAY) {
                        ColorMode mode = TreeEditorElement.getModeForCriteria(criteria, false);
                        if (mode == ColorMode.INVISIBLE) continue;
                    }

                    if (element.isCriteriaVisible() || MODE == EDIT) {
                        if (element.click(mouseX, mouseY, isDoubleClick)) {
                            lastClicked = criteria;
                            return true;
                        }
                    }
                }
            }

            if (isDoubleClick && MODE == EDIT) {
                GuiList.TREE_EDITOR.previous = null;
                GuiList.TREE_EDITOR.selected = null;
                GuiList.TREE_EDITOR.lastClicked = null;
                GuiList.TREE_EDITOR.isDragging = false;
                ITab currentTab = GuiList.TREE_EDITOR.currentTab;
                int offsetX = CORE.getOffsetX();
                ICriteria criteria = RuleHandler.newCriteria(currentTab, UUID.randomUUID(), true);
                criteria.setCoordinates(mouseX - 50 - offsetX, mouseY - 10);
                GuiList.TREE_EDITOR.addCriteria(criteria, mouseX - 50, mouseY - 10, offsetX);
                return true;
            }
            
            return false;
        } else {
            if (lastClicked == null && selected == null) {
                isDragging = true;
                drag = mouseX;
                return true;
            }
        }
        
        return true;
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY) {
        if (isDragging) {
            int difference = mouseX - drag;
            drag = mouseX;

            if (difference != 0) {
                CORE.scroll(difference);
            }
        }

        if (lastClicked != null) {
            for (ICriteria criteria : currentTab.getCriteria()) {
                TreeEditorElement element = getElement(criteria);
                if (element != null) {
                    element.follow(mouseX, mouseY);
                    int wheel = Mouse.getDWheel();
                    if (wheel != 0) {
                        getElement(criteria).scroll(wheel < 0);
                    }
                }
            }
        }
    }
}
