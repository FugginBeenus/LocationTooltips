package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Region protection: gates player actions on the per-region flags resolved via
 * {@link RegionManager#resolveFlag}. Operators always bypass these protections so
 * admins can build/manage inside protected zones.
 *
 * This class registers the "easy tier" handlers that ride on stable Fabric API
 * events (no mixins): block break/place, interaction, container access, entity
 * interaction, and PvP. Explosions / fire spread / mob griefing need mixins and
 * are handled separately.
 */
public class RegionProtection {

    /** Register all event-based protection handlers. */
    public static void register() {
        AttackEntityCallback.EVENT.register(RegionProtection::onPlayerAttackEntity);   // pvp
        PlayerBlockBreakEvents.BEFORE.register(RegionProtection::onBlockBreak);         // block-break
        UseBlockCallback.EVENT.register(RegionProtection::onUseBlock);                  // place / interact / containers
        UseEntityCallback.EVENT.register(RegionProtection::onUseEntity);                // entity-interact
    }

    // ===================== PvP =====================

    /** Prevent PvP damage in regions where PvP is disabled. */
    private static ActionResult onPlayerAttackEntity(
            PlayerEntity attacker, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult) {

        if (world.isClient() || !(attacker instanceof ServerPlayerEntity serverAttacker)) return ActionResult.PASS;
        if (!(target instanceof ServerPlayerEntity targetPlayer)) return ActionResult.PASS;

        var dim = targetPlayer.getWorld().getRegistryKey().getValue();
        BlockPos pos = targetPlayer.getBlockPos();
        RegionManager mgr = RegionManager.of(serverAttacker.server);

        if (!mgr.resolveFlag(dim, pos, RegionFlags.PVP.id)) {
            Region region = mgr.smallestContaining(dim, pos);
            String where = (region != null) ? region.name : "this area";
            serverAttacker.sendMessage(
                    Text.literal("[X] PvP is disabled in ").formatted(Formatting.RED)
                            .append(Text.literal(where).formatted(Formatting.YELLOW))
                            .append(Text.literal("!").formatted(Formatting.RED)),
                    true);
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    // ===================== Block break =====================

    private static boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity be) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) return true;
        if (bypasses(sp)) return true;

        var dim = world.getRegistryKey().getValue();
        if (!RegionManager.of(sp.server).resolveFlag(dim, pos, RegionFlags.BLOCK_BREAK.id)) {
            deny(sp, "break blocks");
            return false; // cancel the break
        }
        return true;
    }

    // ===================== Use block (place / interact / containers) =====================

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
        if (bypasses(sp)) return ActionResult.PASS;

        var dim = world.getRegistryKey().getValue();
        RegionManager mgr = RegionManager.of(sp.server);
        BlockPos clicked = hit.getBlockPos();

        // "interact = deny" is the broad lock: no right-click interactions at all.
        if (!mgr.resolveFlag(dim, clicked, RegionFlags.INTERACT.id)) {
            deny(sp, "interact here");
            return ActionResult.FAIL;
        }

        // Container access (chests, hoppers, furnaces, barrels, crafting/enchant tables...).
        if (isContainer(world, clicked) && !mgr.resolveFlag(dim, clicked, RegionFlags.CONTAINER_ACCESS.id)) {
            deny(sp, "use containers");
            return ActionResult.FAIL;
        }

        // Block placement: holding a BlockItem places at the offset face.
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() instanceof BlockItem) {
            BlockPos placePos = clicked.offset(hit.getSide());
            if (!mgr.resolveFlag(dim, placePos, RegionFlags.BLOCK_PLACE.id)) {
                deny(sp, "place blocks");
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }

    // ===================== Use entity (armor stands, item frames, villagers...) =====================

    private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hit) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
        if (bypasses(sp)) return ActionResult.PASS;

        var dim = world.getRegistryKey().getValue();
        BlockPos pos = entity.getBlockPos();
        if (!RegionManager.of(sp.server).resolveFlag(dim, pos, RegionFlags.ENTITY_INTERACT.id)) {
            deny(sp, "interact with entities");
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    // ===================== Mob spawning (called from MobEntityMixin) =====================

    /** Check if natural mob spawning is allowed at a location. */
    public static boolean canMobSpawn(World world, BlockPos pos) {
        if (world.isClient()) return true;
        var dim = world.getRegistryKey().getValue();
        RegionManager mgr = RegionManager.of(world.getServer());
        return mgr.resolveFlag(dim, pos, RegionFlags.MOB_SPAWNING.id);
    }

    // ===================== helpers =====================

    /** Operators bypass action protections (so admins can build/manage in protected zones). */
    private static boolean bypasses(ServerPlayerEntity p) {
        return p.server.getPlayerManager().isOperator(p.getGameProfile());
    }

    private static boolean isContainer(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof Inventory) return true;
        BlockState state = world.getBlockState(pos);
        return state.createScreenHandlerFactory(world, pos) != null;
    }

    private static void deny(ServerPlayerEntity p, String action) {
        p.sendMessage(Text.literal("You can't " + action + " in this area.").formatted(Formatting.RED), true);
    }
}
