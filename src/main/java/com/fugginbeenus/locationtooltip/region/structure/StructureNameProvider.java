package com.fugginbeenus.locationtooltip.region.structure;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;

import java.util.Optional;

/**
 * Supplies a display name for an auto-tagged structure region. Providers are tried in order
 * (see {@link StructureNaming}); the first non-empty result wins, otherwise the system falls
 * back to {@link StructureNames}. This is the plug-in point for integrations like Waystones.
 */
@FunctionalInterface
public interface StructureNameProvider {
    /**
     * @param server      the server
     * @param dim         the dimension id of the structure
     * @param structureId the structure's registry id (e.g. minecraft:village_plains)
     * @param box         the structure's bounding box
     * @return a name to use, or empty to defer to the next provider / the default name
     */
    Optional<String> nameFor(MinecraftServer server, Identifier dim, Identifier structureId, BlockBox box);
}
