package de.thedead2.progression_reloaded.generation;

import de.thedead2.progression_reloaded.generation.dataprovider.ModLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataEventListener {

    @SubscribeEvent
    public static void onDataGeneration(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookUpProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, MOD_ID, "en_us"));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, MOD_ID, "de_de"));
    }
}
