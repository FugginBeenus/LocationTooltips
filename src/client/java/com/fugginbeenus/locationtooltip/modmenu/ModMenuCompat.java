package com.fugginbeenus.locationtooltip.modmenu;

import com.fugginbeenus.locationtooltip.config.LTConfig;
import com.fugginbeenus.locationtooltip.config.ui.LTStyleOverlayScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public final class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return new ConfigScreenFactory<Screen>() {
            @Override
            public Screen create(Screen parent) {
                // LTStyleOverlayScreen expects (Screen parent, LTConfig cfg)
                return new LTStyleOverlayScreen(parent, LTConfig.get());
            }
        };
    }
}
