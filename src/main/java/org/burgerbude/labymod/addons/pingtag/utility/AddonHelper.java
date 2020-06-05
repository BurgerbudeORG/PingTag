package org.burgerbude.labymod.addons.pingtag.utility;

import com.google.gson.JsonObject;
import net.labymod.addon.AddonLoader;
import net.labymod.api.LabyModAddon;

import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * A helper class that contains important functions for the addon system
 * because you have no other way to get information from other addons.
 *
 * @author Robby
 */
public class AddonHelper {

    private int damageIndicatorScale;
    private int damageIndicatorViewDistance;

    /**
     * Checks if the <b>DamageIndicator</b> addon is installed.
     *
     * @return <b>true</b> if the addon is installed or visible
     */
    public boolean damageIndicatorActive() {
        Class<?> cls = this.forName("net.labymod.addons.damageindicator.DamageIndicator");
        if (cls == null) return false;

        JsonObject configuration = this.configurationByClass(cls);

        this.damageIndicatorScale = configuration.has("scale") ?
                configuration.get("scale").getAsInt() : 100;
        this.damageIndicatorViewDistance = configuration.has("distance") ?
                configuration.get("distance").getAsInt() : 50;

        return configuration.has("visible") && configuration.get("visible").getAsBoolean();
    }

    /**
     * Checks if the <b>FriendTags</b> addon is installed.
     *
     * @return <b>true</b> if the addon is installed or enabled
     */
    public boolean friendTagActive() {
        Class<?> cls = this.forName("de.cerus.friendtags.FriendTags");
        if (cls == null) return false;

        JsonObject configuration = this.configurationByClass(cls);

        return !configuration.has("enabled") || configuration.get("enabled").getAsBoolean();
    }

    /**
     * Gets a specific addon configuration by the class
     *
     * @param addonClass The main addon class
     * @return an addon configuration or an empty {@link JsonObject};
     */
    private JsonObject configurationByClass(Class<?> addonClass) {
        JsonObject object = new JsonObject();
        File targetFile = AddonLoader.getJarFileByClass(addonClass);

        if (targetFile != null) {
            String targetPath = targetFile.getAbsolutePath();
            for (Map.Entry<UUID, File> entry : AddonLoader.getFiles().entrySet()) {
                if (entry.getValue() != null) {
                    String path = entry.getValue().getAbsolutePath();
                    if (path.equals(targetPath)) {
                        LabyModAddon addon = AddonLoader.getAddonByUUID(entry.getKey());
                        object = addon.getConfig();
                        break;
                    }
                }
            }
        }
        return object;
    }

    /**
     * Gets the <b>Class</b> object associated with the class or interface with the given string name.
     *
     * @param name The fully qualified name of the desired class
     * @return the <b>Class</b> object for the class with the specified name or <b>null</b>
     */
    private Class<?> forName(String name) {
        Class<?> cls = null;
        try {
            cls = Class.forName(name);
        } catch (ClassNotFoundException ignored) {
        }
        return cls;
    }

    /**
     * Gets the scale of the <b>DamageIndicator</b> addon
     *
     * @return the scale of the addon
     */
    public int damageIndicatorScale() {
        return this.damageIndicatorScale;
    }

    /**
     * Gets the view distance of the <b>DamageIndicator</b> addon
     *
     * @return the view distance of the addon
     */
    public int damageIndicatorViewDistance() {
        return this.damageIndicatorViewDistance;
    }
}
