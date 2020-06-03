package org.burgerbude.labymod.addons.pingtag;

import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * The main class of the addon
 *
 * @author Robby
 */
public class PingTagAddon extends LabyModAddon {

    private PingDetector pingDetector;

    @Override
    public void onEnable() {
        Minecraft minecraft = Minecraft.getMinecraft();

        this.pingDetector = new PingDetector(minecraft);
    }

    @Override
    public void loadConfig() {

    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {

    }
}
