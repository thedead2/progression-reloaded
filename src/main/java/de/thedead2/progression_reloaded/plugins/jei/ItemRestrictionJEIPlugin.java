package de.thedead2.progression_reloaded.plugins.jei;

import de.thedead2.progression_reloaded.data.abilities.restrictions.ItemRestriction;
import de.thedead2.progression_reloaded.events.LevelEvent;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.thedead2.progression_reloaded.data.abilities.ModRestrictionManagers.ITEM_RESTRICTION_MANAGER;
import static de.thedead2.progression_reloaded.util.ModHelper.*;


@JeiPlugin
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
public class ItemRestrictionJEIPlugin implements IModPlugin {
    private final List<ItemStack> hiddenItems = new ArrayList<>();

    private IJeiRuntime runtime;


    public ItemRestrictionJEIPlugin() {

        if(!isRunningOnServerThread()) {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, LevelEvent.LevelsSyncedEvent.class, e -> this.updateHiddenItems());
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RecipesUpdatedEvent.class, e -> this.updateHiddenItems());
        }
    }


    private void updateHiddenItems() {
        Minecraft.getInstance().execute(() -> {
            if(this.runtime != null) {
                this.runtime.getIngredientManager();

                final long syncStart = System.nanoTime();
                final IIngredientManager ingredients = this.runtime.getIngredientManager();

                LOGGER.debug("Starting to sync JEI with {}.", MOD_NAME);

                this.restoreRestrictedItems(ingredients);
                this.collectRestrictedItems(ingredients);
                this.hideRestrictedItems(ingredients);

                LOGGER.debug("JEI sync complete. Took {}ms.", DECIMAL_FORMAT.format((System.nanoTime() - syncStart) / 1000000));
            }
        });
    }


    private void restoreRestrictedItems(IIngredientManager ingredients) {
        final long restoreStart = System.nanoTime();
        LOGGER.debug("Restoring {} hidden items.", this.hiddenItems.size());

        if(!this.hiddenItems.isEmpty()) {
            ingredients.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, this.hiddenItems);
            this.hiddenItems.clear();
        }

        LOGGER.debug("Items list restored. Took {}ms.", DECIMAL_FORMAT.format((System.nanoTime() - restoreStart) / 1000000));
    }


    private void collectRestrictedItems(IIngredientManager ingredients) {
        final long hideCalcStart = System.nanoTime();
        for(final ItemStack item : ingredients.getAllIngredients(VanillaTypes.ITEM_STACK)) {

            final Pair<Boolean, ItemRestriction> pair = ITEM_RESTRICTION_MANAGER.isRestricted(item.getItem());

            if(pair.getLeft()) {
                ItemRestriction restriction = pair.getRight();
                if(ITEM_RESTRICTION_MANAGER.doesNotHaveLevel(Minecraft.getInstance().player, restriction) && restriction.shouldHideInJEI()) {
                    this.hiddenItems.add(item);
                }
            }
        }

        LOGGER.debug("Marked {} entries for hiding. Took {}ms.", this.hiddenItems.size(), DECIMAL_FORMAT.format((System.nanoTime() - hideCalcStart) / 1000000));
    }


    private void hideRestrictedItems(IIngredientManager ingredients) {
        LOGGER.debug("Hiding {} entries from JEI.", this.hiddenItems.size());
        final long hideStart = System.nanoTime();

        if(!this.hiddenItems.isEmpty()) {
            ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, this.hiddenItems);
        }

        LOGGER.debug("All entries hidden. Took {}ms.", DECIMAL_FORMAT.format((System.nanoTime() - hideStart) / 1000000));
    }


    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(MOD_ID, "item_restrictions_jei_plugin");
    }


    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        this.runtime = jeiRuntime;
    }
}