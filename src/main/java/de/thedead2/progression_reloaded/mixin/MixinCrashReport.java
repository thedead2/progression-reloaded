package de.thedead2.progression_reloaded.mixin;

import de.thedead2.progression_reloaded.ProgressionReloaded;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    @Shadow @Final private Throwable exception;
    @Shadow @Final private List<CrashReportCategory> details;

    @Inject(at = @At("HEAD"), method = "getFriendlyReport")
    public void onFriendlyReport(CallbackInfoReturnable<String> cir){
        CrashHandler crashHandler = CrashHandler.getInstance();
        if(crashHandler.resolveCrash(exception)){
            return;
        }
        details.forEach(crashReportCategory -> {
            AtomicReference<String> errorMessage = new AtomicReference<>();
            crashReportCategory.entries.forEach(crashReportCategory$Entry -> {
                String key = crashReportCategory$Entry.getKey();
                if(key.contains("Exception")){
                    errorMessage.set(crashReportCategory$Entry.getValue());
                    crashHandler.resolveCrash(crashReportCategory.getStacktrace(), errorMessage.get());
                }
                else if(key.contains("Screen")){
                    if(crashReportCategory$Entry.getValue().contains(ProgressionReloaded.MAIN_PACKAGE)){
                        crashHandler.addScreenCrash(crashReportCategory$Entry, exception);
                    }
                }
            });
        });
    }
}
