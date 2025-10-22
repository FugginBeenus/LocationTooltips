package com.fugginbeenus.locationtooltip.item;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.server.network.ServerPlayerEntity;

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
}
