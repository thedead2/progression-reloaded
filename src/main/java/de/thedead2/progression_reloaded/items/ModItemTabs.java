package de.thedead2.progression_reloaded.items;

import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import de.thedead2.progression_reloaded.util.language.TranslationKeyType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItemTabs {

    public static CreativeModeTab PROGRESSION_RELOADED_TAB;


    @SubscribeEvent
    public static void registerTab(final CreativeModeTabEvent.Register event) {
        PROGRESSION_RELOADED_TAB = event.registerCreativeModeTab(
                new ResourceLocation(ModHelper.MOD_ID, "progression_reloaded_tab"),
                builder -> builder
                        .icon(() -> new ItemStack(ModItems.PROGRESSION_BOOK.get()))
                        .title(Component.translatable(TranslationKeyProvider.translationKeyFor(TranslationKeyType.CREATIVE_MODE_TAB, "progression_reloaded_tab")))
        );
    }


    @SubscribeEvent
    public static void addToCreativeModTab(final CreativeModeTabEvent.BuildContents event) {
        if(event.getTab() == PROGRESSION_RELOADED_TAB) {
            event.accept(ModItems.PROGRESSION_BOOK);
            event.accept(ModItems.EXTRA_LIFE);
            event.accept(ModItems.HALF_EXTRA_LIFE);
            event.accept(ModItems.QUARTER_EXTRA_LIFE);
        }
    }
}
