package org.burgerbude.labymod.addons.pingtag.render;

import net.labymod.main.LabyMod;
import net.labymod.user.User;
import net.labymod.user.group.EnumGroupDisplayType;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.burgerbude.labymod.addons.pingtag.PingTagAddon;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * A renderer which renders the ping of an player at a specific position
 *
 * @author Robby
 */
public class PingTagRenderer {

    private final PingTagAddon addon;
    private final Minecraft minecraft;
    private final RenderManager renderManager;
    private final int viewDistance;

    /**
     * Default constructor
     *
     * @param addon     The addon
     * @param minecraft The game
     */
    public PingTagRenderer(PingTagAddon addon, Minecraft minecraft) {
        this.addon = addon;
        this.minecraft = minecraft;
        this.renderManager = minecraft.getRenderManager();
        this.viewDistance = 4096;
    }

    /**
     * Renders a tag above the head of an entity
     *
     * @param entity       The entity to render the tag
     * @param positionX    The x position of the tag
     * @param positionY    The y position of the tag
     * @param positionZ    The z position of the tag
     * @param partialTicks The partial ticks
     */
    public void renderTag(Entity entity, double positionX, double positionY, double positionZ, float partialTicks) {
        if (!(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;

        double distance = player.getDistanceSqToEntity(this.renderManager.livingPlayer);
        int ping = this.addon.pingDetector().playerPing(player.getUniqueID());

        if (player == this.minecraft.thePlayer && this.minecraft.gameSettings.hideGUI || player.isInvisible()) return;

        if (!player.isSneaking() && distance <= this.viewDistance && ping > 0) {

            float fixedPlayerViewX = this.renderManager.playerViewX *
                    (this.minecraft.gameSettings.thirdPersonView == 2 ? -1 : 1);

            FontRenderer fontRenderer = this.addon.getApi().getDrawUtils().getFontRenderer();

            GlStateManager.pushMatrix();

            User user = this.addon.getApi().getUserManager().getUser(player.getUniqueID());
            double height = player.height + .8D;

            if (player.isSneaking()) height += .03D;

            if (user != null) {
                if (LabyMod.getSettings().cosmetics) height += user.getMaxNameTagHeight();
                if (user.getGroup().getDisplayType() == EnumGroupDisplayType.ABOVE_HEAD) height += .129D;
                if (user.getSubTitle() != null) height += user.getSubTitleSize() / 6 - .025D;
            }

            //DamageIndicator Support
            if (this.addon.addonHelper().damageIndicatorActive()) {
                int indicatorViewDistance = this.addon.addonHelper().damageIndicatorViewDistance();

                if (this.viewDistance >= indicatorViewDistance * indicatorViewDistance) {
                    if (player != this.minecraft.thePlayer)
                        height += ((double) this.addon.addonHelper().damageIndicatorScale() / 100) * .23;
                }

            }

            //Friend Tags Support
            if (this.addon.addonHelper().friendTagActive())
                if (player != this.minecraft.thePlayer && this.addon.isFriend(player.getUniqueID()))
                    height += .23;

            //scoreboard hearts
            if (distance < 100) {
                Scoreboard scoreboard = player.getWorldScoreboard();
                ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(2);

                if (scoreObjective != null) height += fontRenderer.FONT_HEIGHT * 1.15D * .026_666_667D;

            }

            height += this.addon.size() / 6 - .25D;

            GlStateManager.translate(positionX, positionY + height, positionZ);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-this.renderManager.playerViewY, .0F, 1.0F, .0F);
            GlStateManager.rotate(fixedPlayerViewX, 1.0F, .0F, .0F);

            float scale = .016F * this.addon.size();
            GlStateManager.scale(-scale, -scale, scale);

            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);

            String text = (this.addon.rainbow() ? "" : colorizedPing(ping)) +
                    (this.addon.shouldPrefix() ? this.addon.displayPrefix() : "") + ping +
                    (this.addon.shouldSuffix() ? this.addon.displaySuffix() : "");

            int textPosition = fontRenderer.getStringWidth(text) / 2;

            switch (this.addon.displayMode()) {
                case ABOVE_HEAD_TEXT:
                    Gui.drawRect(-textPosition - 1, -1, textPosition + 1, 9,
                            new Color(.0F, .0F, .0F, .25F).hashCode());
                    break;
                case ABOVE_HEAD_TEXTURE:
                    Gui.drawRect(-5, -1, 7, 9, new Color(.0F, .0F, .0F, .25F).hashCode());
                    GlStateManager.enableBlend();
                    GlStateManager.depthMask(true);
                    this.drawPingIcon(-4, 0, ping);
                    break;
                case ABOVE_HEAD_TEXT_TEXTURE:
                    Gui.drawRect(-textPosition - 8, -1, textPosition + 4, 9, new Color(.0F, .0F, .0F, .25F).hashCode());
                    GlStateManager.enableBlend();
                    GlStateManager.depthMask(true);
                    this.drawPingIcon(-textPosition - 7, 0, ping);
                    break;
            }

            GlStateManager.enableBlend();
            GlStateManager.depthMask(true);

            int xPosition = this.addon.displayMode() == DisplayMode.ABOVE_HEAD_TEXT_TEXTURE ?
                    -textPosition + 4 :
                    -textPosition;

            if (this.addon.displayMode() != DisplayMode.ABOVE_HEAD_TEXTURE)
                if (this.addon.rainbow()) {
                    switch (this.addon.rainbowMode()) {
                        case NORMAL:
                            fontRenderer.drawString(text, xPosition, 0, rainbow(0));
                            break;
                        case WAVE:
                            this.waveString(fontRenderer, text, xPosition, 0);
                            break;
                    }
                } else
                    fontRenderer.drawString(text, xPosition, 0, -1);

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    /**
     * Colorized the ping of an player
     *
     * @param ping The ping of an player
     * @return a {@link ModColor}
     */
    private ModColor colorizedPing(int ping) {
        if (ping <= 51) return ModColor.DARK_GREEN;
        else if (ping <= 76) return ModColor.GREEN;
        else if (ping <= 125) return ModColor.YELLOW;
        else if (ping <= 250) return ModColor.RED;
        else return ModColor.DARK_RED;
    }

    /**
     * Calculates a color spectrum that resembles a rainbow
     *
     * @param offset The offset of the spectrum
     * @return calculated color
     */
    private int rainbow(int offset) {
        long speed = this.addon.rainbowSpeed() * 1000L;
        float hue = (float) ((System.currentTimeMillis() + offset) % speed) / speed;
        return Color.HSBtoRGB(hue, .8F, .8F);
    }

    /**
     * Renders a rainbow wave into the string
     *
     * @param fontRenderer The renderer for the font
     * @param text         The text to be rendered
     * @param positionX    The x position of the string
     * @param positionY    The y position of the string
     */
    private void waveString(FontRenderer fontRenderer, String text, int positionX, int positionY) {
        for (char c : text.toCharArray()) {
            fontRenderer.drawString(String.valueOf(c), positionX, positionY,
                    rainbow(positionX * -fontRenderer.getStringWidth(text)));
            positionX += fontRenderer.getStringWidth(String.valueOf(c));
        }
    }

    /**
     * Draws a ping icon
     *
     * @param positionX The x position of the icon
     * @param positionY The y position of the icon
     * @param ping      The current ping
     */
    private void drawPingIcon(int positionX, int positionY, int ping) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GuiPlayerTabOverlay.icons);
        int state;

        switch (this.colorizedPing(ping)) {
            case DARK_GREEN:
                state = 0;
                break;
            case GREEN:
                state = 1;
                break;
            case YELLOW:
                state = 2;
                break;
            case RED:
                state = 3;
                break;
            case DARK_RED:
                state = 4;
                break;
            default:
                state = 5;
                break;
        }

        LabyMod.getInstance().getDrawUtils()
                .drawTexturedModalRect(positionX, positionY, 0, 176 + state * 8, 10, 8);
    }

}
