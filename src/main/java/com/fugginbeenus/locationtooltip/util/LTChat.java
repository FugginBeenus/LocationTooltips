package com.fugginbeenus.locationtooltip.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class LTChat {
    private LTChat() {}

    public static void tell(ServerPlayer player, Component message, boolean overlay) {
        if (player == null) return;
        //? if >=26.1 {
        /*if (overlay) {
            player.sendOverlayMessage(message);
        } else {
            player.sendSystemMessage(message);
        }
        *///?} else {
        player.displayClientMessage(message, overlay);
        //?}
    }
}
