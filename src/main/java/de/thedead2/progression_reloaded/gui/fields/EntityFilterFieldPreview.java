package de.thedead2.progression_reloaded.gui.fields;

import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IFilter;
import de.thedead2.progression_reloaded.api.criteria.IRuleProvider;
import de.thedead2.progression_reloaded.api.gui.IDrawHelper;
import de.thedead2.progression_reloaded.api.special.IAdditionalTooltip;
import de.thedead2.progression_reloaded.helpers.EntityHelper;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static de.thedead2.progression_reloaded.api.special.DisplayMode.EDIT;
import static de.thedead2.progression_reloaded.gui.core.GuiList.*;

public class EntityFilterFieldPreview extends ItemFilterField implements IField {
    private final int x;
    private final int y;
    private final float scale;
    protected final int mouseX1;
    protected final int mouseX2;
    protected final int mouseY1;
    protected final int mouseY2;
    private EntityLivingBase entity;
    private int ticker;

    public EntityFilterFieldPreview(String fieldName, Object object, int x, int y, float scale) {
        super(fieldName, object);
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.mouseX1 = x + 5;
        this.mouseX2 = (int) (x + 14 * scale);
        this.mouseY1 = y - (int)(14.2 * scale);
        this.mouseY2 = y;
    }

    public EntityLivingBase getEntity() {
        return entity != null ? entity : MCClientHelper.getPlayer();
    }

    public EntityLivingBase getEntity(boolean hovered) {
        try {
            if (ticker >= 200 || ticker == 0) {
                EntityPlayer player = MCClientHelper.getPlayer();
                IFilter filter = EntityHelper.getFilter(getFilters(), player);
                List<EntityLivingBase> entities = (List<EntityLivingBase>) filter.getRandom(player);
                if (entities.size() > 0) {
                    entity = (EntityLivingBase) EntityList.createEntityByName(EntityHelper.getNameForEntity(entities.get(player.worldObj.rand.nextInt(entities.size()))), player.worldObj);
                    if (entity instanceof EntityLiving) {
                        ((EntityLiving) entity).onInitialSpawn(player.worldObj.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);
                    }

                    filter.apply(entity);
                    ticker = 1;
                }
            }

            if (!hovered) ticker++;
            else if (!GuiScreen.isShiftKeyDown()) ticker += 2;
        } catch (Exception e) {}

        return entity != null ? entity : MCClientHelper.getPlayer();
    }

    @Override
    public void draw(final IRuleProvider provider, final IDrawHelper helper, final int renderX, final int renderY, final int color, final int yPos, final int mouseX, final int mouseY) {
        try {
            boolean hovered = mouseX >= mouseX1 && mouseX <= mouseX2 && mouseY >= mouseY1 && mouseY <= mouseY2;
            EntityLivingBase entity = getEntity(hovered);
            if (hovered) {
                List<String> tooltip = new ArrayList();
                tooltip.add(entity.getName());
                if (object instanceof IAdditionalTooltip) {
                    ((IAdditionalTooltip)object).addHoverTooltip(getFieldName(), entity, tooltip);
                }

                TOOLTIP.add(tooltip);
            }

            final EntityLivingBase entityLivingBase = entity;
            LAST.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    GuiInventory.drawEntityOnScreen(CORE.getOffsetX() + renderX + 24 + x, CORE.screenTop + renderY + y + EntityHelper.getOffsetForEntity(entityLivingBase), EntityHelper.getSizeForEntity(entityLivingBase), 25F, -5F, entityLivingBase);
                    return null;
                }
            });
            //helper.drawStack(renderX, renderY, getEntity(hovered), x, y, scale);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean attemptClick(int mouseX, int mouseY) {
        if (MODE != EDIT) return false;
        boolean clicked = mouseX >= mouseX1 && mouseX <= mouseX2 && mouseY >= mouseY1 && mouseY <= mouseY2;
        if (clicked) {
            return super.click();
        } else return false;
    }
}