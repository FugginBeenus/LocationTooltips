package com.fugginbeenus.locationtooltip.region.structure;

import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Optional;

@FunctionalInterface
public interface StructureNameProvider {
    Optional<String> nameFor(MinecraftServer server, ResourceLocation dim, ResourceLocation structureId, BoundingBox box);
}
