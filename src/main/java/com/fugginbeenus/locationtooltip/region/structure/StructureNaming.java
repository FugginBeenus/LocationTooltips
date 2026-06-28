package com.fugginbeenus.locationtooltip.region.structure;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves the display name for an auto-tagged structure region by asking each registered
 * {@link StructureNameProvider} in turn, then falling back to {@link StructureNames}.
 *
 * Integrations (e.g. Waystones) register a provider at init if their mod is present.
 */
public final class StructureNaming {
    private StructureNaming() {}

    private static final List<StructureNameProvider> PROVIDERS = new ArrayList<>();

    public static void addProvider(StructureNameProvider provider) {
        if (provider != null) PROVIDERS.add(provider);
    }

    public static boolean hasProviders() {
        return !PROVIDERS.isEmpty();
    }

    /** Name from a provider only (no fallback) — used by delayed re-resolution. */
    public static Optional<String> providerName(MinecraftServer server, Identifier dim, Identifier structureId, BlockBox box) {
        for (StructureNameProvider p : PROVIDERS) {
            try {
                Optional<String> name = p.nameFor(server, dim, structureId, box);
                if (name != null && name.isPresent()) return name;
            } catch (Throwable ignored) {
                // A misbehaving integration must never break structure tagging.
            }
        }
        return Optional.empty();
    }

    /** Final name: a provider's name if any, otherwise the built-in structure name. */
    public static String resolve(MinecraftServer server, Identifier dim, Identifier structureId, BlockBox box) {
        return providerName(server, dim, structureId, box)
                .orElseGet(() -> StructureNames.displayName(structureId));
    }
}
