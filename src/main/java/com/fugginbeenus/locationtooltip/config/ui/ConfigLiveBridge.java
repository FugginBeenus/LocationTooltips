// com/fugginbeenus/locationtooltip/config/ui/ConfigLiveBridge.java
package com.fugginbeenus.locationtooltip.config.ui;

import com.fugginbeenus.locationtooltip.config.LTConfig;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Live bridge for ClothConfig v11: while our config screen is open,
 * copy current widget values into LTConfig every client tick.
 */
public final class ConfigLiveBridge {
    private ConfigLiveBridge() {}

    /** link one config entry to a writer into LTConfig */
    public static final class Tracked<T> {
        public final AbstractConfigListEntry<T> entry;
        public final Consumer<T> apply;
        public Tracked(AbstractConfigListEntry<T> entry, Consumer<T> apply) {
            this.entry = entry;
            this.apply = apply;
        }
    }

    private static final List<Tracked<?>> CURRENT = new ArrayList<>();
    private static WeakReference<Object> openScreenRef = new WeakReference<>(null);

    private static boolean initialized = false;

    /** Call once during client init. Safe to call multiple times. */
    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            final Object open = openScreenRef.get();
            if (open == null) return;
            if (client.currentScreen != open) {
                CURRENT.clear();
                openScreenRef.clear();
                return;
            }
            // apply all current values into LTConfig
            LTConfig cfg = LTConfig.get();
            for (Tracked<?> any : CURRENT) {
                apply(any);
            }
            cfg.save(); // persist live
        });
    }

    /** Start a live session for the given screen with all tracked entries. */
    public static void beginSession(Object clothConfigScreen, List<Tracked<?>> tracked) {
        CURRENT.clear();
        if (tracked != null) CURRENT.addAll(tracked);
        openScreenRef = new WeakReference<>(clothConfigScreen);
    }

    @SuppressWarnings("unchecked")
    private static <T> void apply(Tracked<?> any) {
        Tracked<T> t = (Tracked<T>) any;
        T value = t.entry.getValue();
        t.apply.accept(value);
    }
}
