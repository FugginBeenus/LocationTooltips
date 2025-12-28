package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.region.Region;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Handles region protection features:
 * - PvP prevention
 * - Mob spawning prevention
 */
public class RegionProtection {

    /**
     * Register all protection event handlers
     */
    public static void register() {
        // PvP Protection
        AttackEntityCallback.EVENT.register(RegionProtection::onPlayerAttackEntity);
    }

    /**
     * Prevent PvP damage in regions where PvP is disabled
     */
    private static ActionResult onPlayerAttackEntity(
            PlayerEntity attacker,
            World world,
            Hand hand,
            Entity target,
            @Nullable EntityHitResult hitResult) {

        // Only care about server-side and player vs player
        if (world.isClient() || !(attacker instanceof ServerPlayerEntity serverAttacker)) {
            return ActionResult.PASS;
        }

        if (!(target instanceof ServerPlayerEntity targetPlayer)) {
            return ActionResult.PASS;
        }

        // Get region at target's location
        var dim = targetPlayer.getWorld().getRegistryKey().getValue();
        BlockPos pos = targetPlayer.getBlockPos();
        RegionManager mgr = RegionManager.of(serverAttacker.server);
        Region region = mgr.smallestContaining(dim, pos);

        // Check if PvP is disabled in this region
        if (region != null && !region.allowPvP) {
            // Send feedback to attacker
            serverAttacker.sendMessage(
                    Text.literal("⚔️ PvP is disabled in ")
                            .formatted(Formatting.RED)
                            .append(Text.literal(region.name).formatted(Formatting.YELLOW))
                            .append(Text.literal("!").formatted(Formatting.RED)),
                    true  // Show in action bar
            );
            return ActionResult.FAIL;  // Cancel the attack
        }

        return ActionResult.PASS;  // Allow the attack
    }

    /**
     * Check if mob spawning is allowed at a specific location
     * Called from entity spawn events
     */
    public static boolean canMobSpawn(World world, BlockPos pos) {
        if (world.isClient()) return true;

        var dim = world.getRegistryKey().getValue();
        RegionManager mgr = RegionManager.of(world.getServer());
        Region region = mgr.smallestContaining(dim, pos);

        // If in a region, check the mob spawning flag
        if (region != null) {
            return region.allowMobSpawning;
        }

        return true;  // Allow spawning outside regions
    }
}