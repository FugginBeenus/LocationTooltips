package com.fugginbeenus.locationtooltip.item;

import com.fugginbeenus.locationtooltip.region.SelectionManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A special admin/player tool used to select region corners and name regions.
 * - Right-click: set first corner
 * - Left-click: set second corner
 * - Shift+Right-click: open region naming UI
 */
public class RegionWandItem extends Item {

    public RegionWandItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide) return InteractionResult.SUCCESS;

        var player = (ServerPlayer) context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        boolean sneaking = player.isShiftKeyDown();

        if (sneaking && SelectionManager.hasBoth(player)) {
            SelectionManager.openNamingScreen(player);
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        if (!SelectionManager.hasBoth(player)) {
            if (SelectionManager.getFirst(player) == null) {
                SelectionManager.setFirst(player, pos);
                player.displayClientMessage(Component.literal("First corner set at " + pos.toShortString()), true);
                player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0f, 1.5f);
            } else {
                SelectionManager.setSecond(player, pos);
                player.displayClientMessage(Component.literal("Second corner set at " + pos.toShortString()), true);
                player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.3f);
            }
        } else {
            SelectionManager.clear(player);
            SelectionManager.setFirst(player, pos);
            player.displayClientMessage(Component.literal("Selection reset. First corner set at " + pos.toShortString()), true);
            player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0f, 1.0f);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (!world.isClientSide && player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            if (SelectionManager.hasBoth(serverPlayer)) {
                SelectionManager.openNamingScreen(serverPlayer);
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    //? if >=1.21 {
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, net.minecraft.world.item.TooltipFlag type) {
    *///?} else {
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, net.minecraft.world.item.TooltipFlag context) {
    //?}
        tooltip.add(Component.literal("Select corners to create regions").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Right-click: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("Set corner").withStyle(ChatFormatting.WHITE)));
        tooltip.add(Component.literal("Shift + Right-click: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("Create region").withStyle(ChatFormatting.WHITE)));
    }
}