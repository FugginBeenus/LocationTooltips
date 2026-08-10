package com.fugginbeenus.locationtooltip.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * Whether someone may manage every region rather than just their own. 1.21.11 replaced the
 * numeric op levels with a permission set, so both shapes live here instead of at each call site.
 */
public final class LTPerms {
    private LTPerms() {}

    public static boolean isAdmin(ServerPlayer player) {
        if (player == null) return false;
        //? if >=1.21.11 {
        /*return player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
        *///?} else {
        return player.level().getServer().getPlayerList().isOp(player.getGameProfile());
        //?}
    }

    public static boolean isAdmin(CommandSourceStack source) {
        //? if >=1.21.11 {
        /*return source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
        *///?} else {
        return source.hasPermission(2);
        //?}
    }
}
