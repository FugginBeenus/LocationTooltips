package com.fugginbeenus.locationtooltip.item;

import com.fugginbeenus.locationtooltip.region.SelectionManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A special admin/player tool used to select region corners and name regions.
 * - Right-click: set first corner
 * - Left-click: set second corner
 * - Shift+Right-click: open region naming UI
 */
public class RegionWandItem extends Item {

    public RegionWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        var player = (ServerPlayerEntity) context.getPlayer();
        if (player == null) return ActionResult.PASS;

        BlockPos pos = context.getBlockPos();
        boolean sneaking = player.isSneaking();

        if (sneaking && SelectionManager.hasBoth(player)) {
            SelectionManager.openNamingScreen(player);
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.2f);
            return ActionResult.SUCCESS;
        }

        if (!SelectionManager.hasBoth(player)) {
            if (SelectionManager.getFirst(player) == null) {
                SelectionManager.setFirst(player, pos);
                player.sendMessage(Text.literal("First corner set at " + pos.toShortString()), true);
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), SoundCategory.PLAYERS, 1.0f, 1.5f);
            } else {
                SelectionManager.setSecond(player, pos);
                player.sendMessage(Text.literal("Second corner set at " + pos.toShortString()), true);
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.PLAYERS, 1.0f, 1.3f);
            }
        } else {
            SelectionManager.clear(player);
            SelectionManager.setFirst(player, pos);
            player.sendMessage(Text.literal("Selection reset. First corner set at " + pos.toShortString()), true);
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity player, Hand hand) {
        if (!world.isClient && player.isSneaking() && player instanceof ServerPlayerEntity serverPlayer) {
            if (SelectionManager.hasBoth(serverPlayer)) {
                SelectionManager.openNamingScreen(serverPlayer);
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                return TypedActionResult.success(player.getStackInHand(hand));
            }
        }
        return TypedActionResult.pass(player.getStackInHand(hand));
    }
}
