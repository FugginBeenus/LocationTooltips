package com.fugginbeenus.locationtooltip.item;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide && player instanceof ServerPlayer sp) {
            // Tell client to open the panel
            LTPackets.openAdminPanel(sp);

            // feedback
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.2f);
            player.displayClientMessage(Component.literal("Opening Admin Panel..."), true);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.consume(stack); // allow client hand animation
    }

    @Override
    //? if >=1.21 {
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, net.minecraft.world.item.TooltipFlag type) {
    *///?} else {
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, net.minecraft.world.item.TooltipFlag context) {
    //?}
        tooltip.add(Component.literal("Manage your regions").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Players: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("View your regions").withStyle(ChatFormatting.WHITE)));
        tooltip.add(Component.literal("Admins: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("View all regions").withStyle(ChatFormatting.WHITE)));
    }
}