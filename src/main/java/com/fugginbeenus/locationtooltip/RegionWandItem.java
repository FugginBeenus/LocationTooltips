package com.fugginbeenus.locationtooltip;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

/** Handles the wand right-click on a block (no Fabric callback required). */
public class RegionWandItem extends Item {
    public RegionWandItem(Settings settings) { super(settings); }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        PlayerEntity player = ctx.getPlayer();
        if (player == null) return ActionResult.PASS;

        // Server: consume to prevent block handling; UX is client-only.
        if (!world.isClient) return ActionResult.SUCCESS;

        // Synthesize a BlockHitResult compatible with our bridge
        BlockHitResult hit = new BlockHitResult(
                ctx.getHitPos(),
                ctx.getSide(),
                ctx.getBlockPos(),
                ctx.hitsInsideBlock()
        );

        // Call client bridge via reflection so common sources don't hard-ref client
        try {
            Class<?> bridge = Class.forName("com.fugginbeenus.locationtooltip.client.ClientWandBridge");
            var method = bridge.getMethod("handleUseBlock",
                    PlayerEntity.class, World.class, Hand.class, BlockHitResult.class);
            method.invoke(null, player, world, ctx.getHand(), hit);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return ActionResult.SUCCESS;
    }
}
