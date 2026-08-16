package com.fugginbeenus.locationtooltip.region.structure;

import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StructureNaming {
    private StructureNaming() {}

    private static final List<StructureNameProvider> PROVIDERS = new ArrayList<>();

    public static void addProvider(StructureNameProvider provider) {
        if (provider != null) PROVIDERS.add(provider);
    }

    public static boolean hasProviders() {
        return !PROVIDERS.isEmpty();
    }

    public static Optional<String> providerName(MinecraftServer server, ResourceLocation dim, ResourceLocation structureId, BoundingBox box) {
        for (StructureNameProvider p : PROVIDERS) {
            try {
                Optional<String> name = p.nameFor(server, dim, structureId, box);
                if (name != null && name.isPresent()) return name;
            } catch (Throwable ignored) {
            }
        }
        return Optional.empty();
    }

    public static String resolve(MinecraftServer server, ResourceLocation dim, ResourceLocation structureId, BoundingBox box) {
        return providerName(server, dim, structureId, box)
                .orElseGet(() -> StructureNames.displayName(structureId));
    }
}
