package com.fugginbeenus.locationtooltip.modmenu;

import com.fugginbeenus.locationtooltip.config.LTConfig;
import com.fugginbeenus.locationtooltip.config.ui.LTStyleOverlayScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> new LTStyleOverlayScreen(parent, LTConfig.get());
    }
}
