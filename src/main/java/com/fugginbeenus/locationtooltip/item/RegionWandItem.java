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
//? if <1.21.11 {
import net.minecraft.world.InteractionResultHolder;
//?}
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
        if (world.isClientSide()) return InteractionResult.SUCCESS;

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
                com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("First corner set at " + pos.toShortString()), true);
                player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0f, 1.5f);
            } else {
                SelectionManager.setSecond(player, pos);
                com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("Second corner set at " + pos.toShortString()), true);
                player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.3f);
            }
        } else {
            SelectionManager.clear(player);
            SelectionManager.setFirst(player, pos);
            com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("Selection reset. First corner set at " + pos.toShortString()), true);
            player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0f, 1.0f);
        }

        return InteractionResult.SUCCESS;
    }

    //? if >=1.21.11 {
    /*@Override
    public InteractionResult use(Level world, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        return openNaming(world, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
    *///?} else {
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        return openNaming(world, player)
                ? InteractionResultHolder.success(player.getItemInHand(hand))
                : InteractionResultHolder.pass(player.getItemInHand(hand));
    }
    //?}

    private static boolean openNaming(Level world, net.minecraft.world.entity.player.Player player) {
        if (world.isClientSide() || !player.isShiftKeyDown()) return false;
        if (!(player instanceof ServerPlayer serverPlayer) || !SelectionManager.hasBoth(serverPlayer)) return false;
        SelectionManager.openNamingScreen(serverPlayer);
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        return true;
    }

    //? if >=1.21.11 {
    /*@Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> out,
                                net.minecraft.world.item.TooltipFlag type) {
        ltTooltip(out);
    }
    *///?} elif >=1.21 {
    /*@Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
                                List<Component> tooltip, net.minecraft.world.item.TooltipFlag type) {
        ltTooltip(tooltip::add);
    }
    *///?} else {
    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip,
                                net.minecraft.world.item.TooltipFlag context) {
        ltTooltip(tooltip::add);
    }
    //?}

    private static void ltTooltip(java.util.function.Consumer<Component> out) {
        out.accept(Component.literal("Select corners to create regions").withStyle(ChatFormatting.GRAY));
        out.accept(Component.literal(""));
        out.accept(Component.literal("Right-click: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("Set corner").withStyle(ChatFormatting.WHITE)));
        out.accept(Component.literal("Shift + Right-click: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("Create region").withStyle(ChatFormatting.WHITE)));
    }
}