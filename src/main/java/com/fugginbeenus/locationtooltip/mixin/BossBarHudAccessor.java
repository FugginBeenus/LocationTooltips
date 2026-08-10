package com.fugginbeenus.locationtooltip.mixin;

import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

/**
 * Exposes {@code BossHealthOverlay.events} so the HUD can tell whether a boss bar is currently
 * showing (they render at the top-centre and would otherwise be covered by the region pill).
 *
 * Client-only — registered in the "client" list of locationtooltip.mixins.json.
 */
@Mixin(BossHealthOverlay.class)
public interface BossBarHudAccessor {
    @Accessor("events")
    Map<UUID, LerpingBossEvent> getBossBars();
}
