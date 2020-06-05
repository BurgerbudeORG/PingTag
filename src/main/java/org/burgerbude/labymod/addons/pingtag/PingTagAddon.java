package org.burgerbude.labymod.addons.pingtag;

import com.google.gson.JsonObject;
import net.labymod.api.LabyModAddon;
import net.labymod.api.events.ServerMessageEvent;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.labyconnect.user.ChatUser;
import net.labymod.main.LabyMod;
import net.labymod.main.lang.LanguageManager;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import org.burgerbude.labymod.addons.pingtag.element.BetterSliderElement;
import org.burgerbude.labymod.addons.pingtag.render.DisplayMode;
import org.burgerbude.labymod.addons.pingtag.render.PingTagRenderer;
import org.burgerbude.labymod.addons.pingtag.render.RainbowMode;
import org.burgerbude.labymod.addons.pingtag.utility.AddonHelper;

import java.util.List;
import java.util.UUID;

/**
 * The main class of the addon
 *
 * @author Robby
 */
public class PingTagAddon extends LabyModAddon {

    private AddonHelper addonHelper;

    private PingDetector pingDetector;
    private PingTagRenderer pingTagRenderer;

    private boolean allow;
    private boolean enable;

    private String displayPrefix;
    private boolean shouldPrefix;

    private String displaySuffix;
    private boolean shouldSuffix;

    private DisplayMode displayMode;
    private float size;

    private boolean rainbow;
    private int rainbowSpeed;
    private RainbowMode rainbowMode;

    @Override
    public void onEnable() {
        this.allow = true;
        Minecraft minecraft = Minecraft.getMinecraft();

        this.addonHelper = new AddonHelper();

        this.pingDetector = new PingDetector(minecraft);
        this.pingTagRenderer = new PingTagRenderer(this, minecraft);

        //Listens to the plugin channel to enable or disable this addon
        this.getApi().getEventManager().register((ServerMessageEvent) (channel, jsonElement) -> {
            if (!channel.equals("pingtag")) return;
            JsonObject object = jsonElement.getAsJsonObject();

            this.allow = !object.has("allowed") || object.get("allowed").getAsBoolean();

            if (!this.allow)
                this.api.displayMessageInChat("§8[§6§lPing§eTag§8] §c§l" + this.translate("not_allowed",
                        "PingTag isn't allowed on this server!"));
        });

        //After quitting a server sets the addon to allowed
        this.getApi().getEventManager().registerOnQuit(serverData -> this.allow = true);

        //Listens on the PlayerListItem packet
        this.getApi().getEventManager().registerOnIncomingPacket(packet -> {
            if (!this.enable || !this.allow) {
                System.out.println("enable -> " + enable + " | allowed -> " + allow);
                return;
            }
            if (packet instanceof SPacketPlayerListItem) {
                SPacketPlayerListItem packetPlayerList = (SPacketPlayerListItem) packet;

                switch (packetPlayerList.getAction()) {
                    case ADD_PLAYER:
                    case UPDATE_LATENCY:
                        this.pingDetector.updatePing();
                        break;
                    default:
                        break;
                }

            }
        });

        //Renders the ping tag
        this.getApi().getEventManager().register((entity, positionX, positionY, positionZ, partialTicks) -> {
            if (!this.enable || !this.allow) {
                System.out.println("enable -> " + enable + " | allowed -> " + allow);
                return;
            }
            this.pingTagRenderer.renderTag(entity, positionX, positionY, positionZ, partialTicks);
        });
    }

    @Override
    public void loadConfig() {
        this.enable = !this.getConfig().has("enable") || this.getConfig().get("enable").getAsBoolean();

        this.displayPrefix = this.getConfig().has("displayPrefix") ?
                this.getConfig().get("displayPrefix").getAsString() : "";
        this.shouldPrefix = this.getConfig().has("shouldPrefix") &&
                this.getConfig().get("shouldPrefix").getAsBoolean();

        this.displaySuffix = this.getConfig().has("displaySuffix") ?
                this.getConfig().get("displaySuffix").getAsString() : " ms";
        this.shouldSuffix = !this.getConfig().has("shouldSuffix") ||
                this.getConfig().get("shouldSuffix").getAsBoolean();

        try {
            this.displayMode = this.getConfig().has("displayMode") ?
                    DisplayMode.valueOf(this.getConfig().get("displayMode").getAsString()) : DisplayMode.ABOVE_HEAD_TEXT;
        } catch (IllegalArgumentException | NullPointerException e) {
            this.displayMode = DisplayMode.ABOVE_HEAD_TEXT;
        }

        this.size = this.getConfig().has("size") ? this.getConfig().get("size").getAsFloat() : 1.6F;

        try {
            this.rainbowMode = this.getConfig().has("rainbowMode") ?
                    RainbowMode.valueOf(this.getConfig().get("rainbowMode").getAsString()) : RainbowMode.NORMAL;
        } catch (IllegalArgumentException | NullPointerException e) {
            this.rainbowMode = RainbowMode.NORMAL;
        }

        this.rainbow = this.getConfig().has("rainbow") && this.getConfig().get("rainbow").getAsBoolean();
        this.rainbowSpeed = this.getConfig().has("rainbowSpeed") ?
                this.getConfig().get("rainbowSpeed").getAsInt() : 12;
    }

    @Override
    protected void fillSettings(List<SettingsElement> subSettings) {

        //Creates a toggle element to enable or disable the addon
        BooleanElement enableAddonElement = new BooleanElement(
                this.translate("toggle_addon", "Enable PingTag"), this,
                new ControlElement.IconData(Material.LEVER), "enable", this.enable);
        enableAddonElement.addCallback(callback -> {
            this.enable = callback;
            this.getConfig().addProperty("enable", this.enable);
            saveConfig();
        });

        subSettings.add(enableAddonElement);

        //Creates a text element which contains the custom prefix
        StringElement displayPrefixElement = new StringElement(
                this.translate("display_prefix", "Ping Prefix"), this,
                new ControlElement.IconData(Material.SIGN), "displayPrefix", this.displayPrefix);

        displayPrefixElement.addCallback(callback -> {
            this.displayPrefix = callback;
            getConfig().addProperty("displayPrefix", this.displayPrefix);
            saveConfig();
        });

        subSettings.add(displayPrefixElement);

        //Creates a toggle element to enable or disable the prefix
        BooleanElement shouldPrefixElement = new BooleanElement(
                this.translate("should_prefix", "Enable Prefix"), this,
                new ControlElement.IconData(Material.LEVER), "shouldPrefix", this.shouldPrefix);

        shouldPrefixElement.addCallback(callback -> {
            this.shouldPrefix = callback;
            getConfig().addProperty("shouldPrefix", shouldPrefix);
            saveConfig();
        });

        subSettings.add(shouldPrefixElement);

        //Creates a text element which contains the custom suffix
        StringElement displaySuffixElement = new StringElement(
                this.translate("display_suffix", "Ping Suffix"), this,
                new ControlElement.IconData(Material.SIGN), "displaySuffix", this.displaySuffix);

        displaySuffixElement.addCallback(callback -> {
            this.displaySuffix = callback;
            getConfig().addProperty("displaySuffix", this.displaySuffix);
            saveConfig();
        });

        subSettings.add(displaySuffixElement);

        //Creates a toggle element to enable or disable the suffix
        BooleanElement shouldSuffixElement = new BooleanElement(
                this.translate("should_suffix", "Enable Suffix"), this,
                new ControlElement.IconData(Material.LEVER), "shouldSuffix", this.shouldSuffix);

        shouldSuffixElement.addCallback(callback -> {
            this.shouldSuffix = callback;
            getConfig().addProperty("shouldSuffix", shouldSuffix);
            saveConfig();
        });

        subSettings.add(shouldSuffixElement);

        //Creates a slider which control the size of the tag
        BetterSliderElement sliderSizeElement = new BetterSliderElement(
                this.translate("tag_size", "Tag Size"), this,
                new ControlElement.IconData(Material.NAME_TAG), "size", this.size);

        sliderSizeElement.range(0.6F, 1.6F);

        sliderSizeElement.addCallback(callback -> {
            this.size = callback;
            this.getConfig().addProperty("size", this.size);
            saveConfig();
        });

        subSettings.add(sliderSizeElement);

        //Creates a drop down menu which contains all display modes
        DropDownMenu<DisplayMode> displayModeDropDownMenu =
                new DropDownMenu<>(this.translate("display_mode", "Display Mode"),
                        0, 0, 0, 0);
        displayModeDropDownMenu.fill(DisplayMode.values());

        DropDownElement<DisplayMode> displayModeDropDownElement =
                new DropDownElement<>(this.translate("display_mode", "Display Mode"),
                        displayModeDropDownMenu);

        displayModeDropDownMenu.setSelected(this.displayMode);
        displayModeDropDownElement.setChangeListener(callback -> {
            this.displayMode = callback;
            getConfig().addProperty("displayMode", this.displayMode.name());
            saveConfig();
        });

        displayModeDropDownMenu.setEntryDrawer((object, x, y, trimmedEntry) -> {
            if (!(object instanceof DisplayMode)) return;
            DisplayMode displayMode = (DisplayMode) object;
            LabyMod.getInstance().getDrawUtils().drawString(
                    this.translate(displayMode.name().toLowerCase(), displayMode.displayName()), x, y);
        });

        subSettings.add(displayModeDropDownElement);

        subSettings.add(new HeaderElement(this.translate("rainbow_settings", "Rainbow Settings")));

        //Creates a toggle element to enable or disable the rainbow
        BooleanElement rainbowElement = new BooleanElement(
                this.translate("toggle_rainbow", "Enable Rainbow"), this,
                new ControlElement.IconData(Material.LEVER), "rainbow", this.rainbow);

        subSettings.add(rainbowElement);

        //Creates a slider for the speed of the rainbow spectrum
        SliderElement rainbowSpeedElement = new SliderElement(
                this.translate("rainbow_speed", "Rainbow Speed"),
                this, new ControlElement.IconData(Material.SUGAR), "rainbowSpeed", rainbowSpeed);
        rainbowSpeedElement.setRange(1, 25);
        rainbowSpeedElement.addCallback(callback -> {
            this.rainbowSpeed = callback;
            getConfig().addProperty("rainbowSpeed", this.rainbowSpeed);
            saveConfig();
        });

        subSettings.add(rainbowSpeedElement);

        //Creates a drop down menu which contains all rainbow modes
        DropDownMenu<RainbowMode> rainbowModeDropDownMenu = new DropDownMenu<>(
                this.translate("rainbow_mode", "Rainbow Mode"), 0, 0, 0, 0);
        rainbowModeDropDownMenu.fill(RainbowMode.values());

        DropDownElement<RainbowMode> rainbowModeDropDownElement =
                new DropDownElement<>(this.translate("rainbow_mode", "Rainbow Mode"),
                        rainbowModeDropDownMenu);
        rainbowModeDropDownMenu.setSelected(this.rainbowMode);
        rainbowModeDropDownElement.setChangeListener(callback -> {
            this.rainbowMode = callback;
            getConfig().addProperty("rainbowMode", this.rainbowMode.name());
            saveConfig();
        });

        rainbowModeDropDownMenu.setEntryDrawer((object, x, y, trimmedEntry) -> {
            String entry = object.toString().substring(0, 1) + object.toString().substring(1).toLowerCase();
            LabyMod.getInstance().getDrawUtils().drawString(entry, x, y);
        });

        subSettings.add(rainbowModeDropDownElement);
    }

    /**
     * Translates the given key
     *
     * @param key      The key of the translation
     * @param fallback The fallback if the key doesn't translated
     * @return a translated key or the fallback
     */
    public String translate(String key, String fallback) {
        key = "pingtag_" + key;
        String translate = LanguageManager.translate(key);
        return translate.equals(key) ? fallback : translate;
    }

    /**
     * Checks if one of your friends has the unique identifier
     *
     * @param uniqueId The unique identifier to be checked
     * @return <b>true</b> if the player with the unique identifier your friend
     */
    public boolean isFriend(UUID uniqueId) {
        return !this.api.getLabyModChatClient().getFriends().isEmpty() &&
                this.api.getLabyModChatClient().getFriends()
                        .stream()
                        .anyMatch(friend -> friend.getGameProfile().getId().equals(uniqueId));
    }

    public AddonHelper addonHelper() {
        return this.addonHelper;
    }

    public boolean allow() {
        return this.allow;
    }

    public boolean enable() {
        return this.enable;
    }

    public String displayPrefix() {
        return this.displayPrefix;
    }

    public boolean shouldPrefix() {
        return this.shouldPrefix;
    }

    public String displaySuffix() {
        return this.displaySuffix;
    }

    public boolean shouldSuffix() {
        return this.shouldSuffix;
    }

    public DisplayMode displayMode() {
        return this.displayMode;
    }

    public float size() {
        return this.size;
    }

    public boolean rainbow() {
        return this.rainbow;
    }

    public int rainbowSpeed() {
        return this.rainbowSpeed;
    }

    public RainbowMode rainbowMode() {
        return this.rainbowMode;
    }

    public PingDetector pingDetector() {
        return this.pingDetector;
    }
}
