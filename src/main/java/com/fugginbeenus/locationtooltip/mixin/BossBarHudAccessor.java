package com.fugginbeenus.locationtooltip.mixin;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

/**
 * Exposes {@code BossBarHud.bossBars} so the HUD can tell whether a boss bar is currently
 * showing (they render at the top-centre and would otherwise be covered by the region pill).
 *
 * Client-only — registered in the "client" list of locationtooltip.mixins.json.
 */
@Mixin(BossBarHud.class)
public interface BossBarHudAccessor {
    @Accessor("bossBars")
    Map<UUID, ClientBossBar> getBossBars();
}
