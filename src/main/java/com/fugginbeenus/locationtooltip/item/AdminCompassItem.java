package com.fugginbeenus.locationtooltip.item;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * Admin-only item:
 *  - Server side: sends S2C to open the Admin Panel, and triggers a nearby list refresh.
 *  - No client imports here, so it's safe for dedicated servers.
 */
public class AdminCompassItem extends Item {

    public AdminCompassItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient && player instanceof ServerPlayerEntity sp) {
            // Tell client to open the panel
            LTPackets.openAdminPanel(sp);

            // feedback
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.2f);
            player.sendMessage(Text.literal("Opening Admin Panel..."), true);
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.consume(stack); // allow client hand animation
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("Manage your regions").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Players: ").formatted(Formatting.YELLOW)
                .append(Text.literal("View your regions").formatted(Formatting.WHITE)));
        tooltip.add(Text.literal("Admins: ").formatted(Formatting.YELLOW)
                .append(Text.literal("View all regions").formatted(Formatting.WHITE)));
    }
}