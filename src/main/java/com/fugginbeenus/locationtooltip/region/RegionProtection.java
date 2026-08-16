package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RegionProtection {
    public static void register() {
        AttackEntityCallback.EVENT.register(RegionProtection::onPlayerAttackEntity);
        PlayerBlockBreakEvents.BEFORE.register(RegionProtection::onBlockBreak);
        UseBlockCallback.EVENT.register(RegionProtection::onUseBlock);
        UseEntityCallback.EVENT.register(RegionProtection::onUseEntity);
    }

    private static InteractionResult onPlayerAttackEntity(
            Player attacker, Level world, InteractionHand hand, Entity target, @Nullable EntityHitResult hitResult) {
        if (world.isClientSide() || !(attacker instanceof ServerPlayer serverAttacker)) return InteractionResult.PASS;
        if (!(target instanceof ServerPlayer targetPlayer)) return InteractionResult.PASS;

        var dim = targetPlayer.level().dimension().location();
        BlockPos pos = targetPlayer.blockPosition();
        RegionManager mgr = RegionManager.of(serverAttacker.level().getServer());

        if (!mgr.resolveFlag(dim, pos, RegionFlags.PVP.id)) {
            Region region = mgr.smallestContaining(dim, pos);
            String where = (region != null) ? region.name : "this area";
            com.fugginbeenus.locationtooltip.util.LTChat.tell(serverAttacker, 
                    Component.literal("[X] PvP is disabled in ").withStyle(ChatFormatting.RED)
                            .append(Component.literal(where).withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal("!").withStyle(ChatFormatting.RED)),
                    true);
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    private static boolean onBlockBreak(Level world, Player player, BlockPos pos, BlockState state, BlockEntity be) {
        if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return true;
        if (bypasses(sp)) return true;

        var dim = world.dimension().location();
        if (!RegionManager.of(sp.level().getServer()).resolveFlag(dim, pos, RegionFlags.BLOCK_BREAK.id)) {
            deny(sp, "break blocks");
            return false;
        }
        return true;
    }

    private static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
        if (bypasses(sp)) return InteractionResult.PASS;

        var dim = world.dimension().location();
        RegionManager mgr = RegionManager.of(sp.level().getServer());
        BlockPos clicked = hit.getBlockPos();

        if (!mgr.resolveFlag(dim, clicked, RegionFlags.INTERACT.id)) {
            deny(sp, "interact here");
            return InteractionResult.FAIL;
        }

        if (isContainer(world, clicked) && !mgr.resolveFlag(dim, clicked, RegionFlags.CONTAINER_ACCESS.id)) {
            deny(sp, "use containers");
            return InteractionResult.FAIL;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof BlockItem) {
            BlockPos placePos = clicked.relative(hit.getDirection());
            if (!mgr.resolveFlag(dim, placePos, RegionFlags.BLOCK_PLACE.id)) {
                deny(sp, "place blocks");
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult onUseEntity(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hit) {
        if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
        if (bypasses(sp)) return InteractionResult.PASS;

        var dim = world.dimension().location();
        BlockPos pos = entity.blockPosition();
        if (!RegionManager.of(sp.level().getServer()).resolveFlag(dim, pos, RegionFlags.ENTITY_INTERACT.id)) {
            deny(sp, "interact with entities");
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static boolean canMobSpawn(Level world, BlockPos pos) {
        if (world.isClientSide()) return true;
        var dim = world.dimension().location();
        RegionManager mgr = RegionManager.of(world.getServer());
        return mgr.resolveFlag(dim, pos, RegionFlags.MOB_SPAWNING.id);
    }

    private static boolean bypasses(ServerPlayer p) {
        return com.fugginbeenus.locationtooltip.util.LTPerms.isAdmin(p);
    }

    private static boolean isContainer(Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof Container) return true;
        BlockState state = world.getBlockState(pos);
        return state.getMenuProvider(world, pos) != null;
    }

    private static void deny(ServerPlayer p, String action) {
        com.fugginbeenus.locationtooltip.util.LTChat.tell(p, Component.literal("You can't " + action + " in this area.").withStyle(ChatFormatting.RED), true);
    }
}
