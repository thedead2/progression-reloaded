package de.thedead2.progression_reloaded.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation if you want to mark a class to be excluded from the automatic event bus registration.
 * Used for triggers and abilities without static @SubscribeEvent methods.
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeFromEventBus {
}