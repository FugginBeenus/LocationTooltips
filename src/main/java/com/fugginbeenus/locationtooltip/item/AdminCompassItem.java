package com.fugginbeenus.locationtooltip.item;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
//? if <1.21.11 {
import net.minecraft.world.InteractionResultHolder;
//?}
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Admin-only item:
 *  - Server side: sends S2C to open the Admin Panel, and triggers a nearby list refresh.
 *  - No client imports here, so it's safe for dedicated servers.
 */
public class AdminCompassItem extends Item {

    public AdminCompassItem(Properties settings) {
        super(settings);
    }

    //? if >=1.21.11 {
    /*@Override
    public net.minecraft.world.InteractionResult use(Level world, Player player, InteractionHand hand) {
        return openPanel(world, player)
                ? net.minecraft.world.InteractionResult.SUCCESS
                : net.minecraft.world.InteractionResult.CONSUME;
    }
    *///?} else {
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return openPanel(world, player)
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.consume(stack); // consume still plays the hand animation
    }
    //?}

    private static boolean openPanel(Level world, Player player) {
        if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return false;
        LTPackets.openAdminPanel(sp);
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.2f);
        com.fugginbeenus.locationtooltip.util.LTChat.tell(sp, Component.literal("Opening Admin Panel..."), true);
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
        out.accept(Component.literal("Manage your regions").withStyle(ChatFormatting.GRAY));
        out.accept(Component.literal(""));
        out.accept(Component.literal("Players: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("View your regions").withStyle(ChatFormatting.WHITE)));
        out.accept(Component.literal("Admins: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("View all regions").withStyle(ChatFormatting.WHITE)));
    }
}