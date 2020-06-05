package org.burgerbude.labymod.addons.pingtag.element;

import net.labymod.api.LabyModAddon;
import net.labymod.main.LabyMod;
import net.labymod.main.ModSettings;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.function.Consumer;

/**
 * A slider element which allow to use decimal numbers
 *
 * @author Robby
 */
public class BetterSliderElement extends ControlElement {

    private Float currentValue;

    private final Consumer<Float> changeCallback;
    private Consumer<Float> callback;

    private float minValue;
    private float maxValue;

    private boolean dragging;
    private boolean hovered;

    private float dragValue;
    private float steps;

    /**
     * Default constructor
     *
     * @param elementName     The name of the element
     * @param configEntryName The entry name of the configuration
     * @param iconData        The data of the icon
     */
    public BetterSliderElement(String elementName, String configEntryName, IconData iconData) {
        super(elementName, configEntryName, iconData);
        this.minValue = 0;
        this.maxValue = 10;
        this.steps = 0.1F;
        if (!configEntryName.isEmpty()) {
            try {
                this.currentValue = (Float) ModSettings.class.getDeclaredField(configEntryName)
                        .get(LabyMod.getSettings());
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        if (this.currentValue == null) this.currentValue = this.minValue;

        this.changeCallback = accepted -> {
            try {
                ModSettings.class.getDeclaredField(configEntryName).set(LabyMod.getSettings(), accepted);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * Default constructor
     *
     * @param displayName  The display name of the element
     * @param iconData     The data of the icon
     * @param currentValue The current value of the element
     */
    public BetterSliderElement(String displayName, IconData iconData, float currentValue) {
        super(displayName, null, iconData);
        this.minValue = 0;
        this.maxValue = 10;
        this.steps = 0.1F;

        this.currentValue = currentValue;
        this.changeCallback = accepted -> {
            if (callback != null) callback.accept(accepted);
        };
    }

    /**
     * Default constructor
     *
     * @param displayName  The display name of the element
     * @param addon        The addon of the element
     * @param iconData     The data of the icon
     * @param attribute    The configuration attribute
     * @param currentValue The current value
     */
    public BetterSliderElement(String displayName, LabyModAddon addon, IconData iconData, String attribute, float currentValue) {
        super(displayName, iconData);
        this.minValue = 0;
        this.maxValue = 10;
        this.steps = 0.1F;

        this.currentValue = currentValue;


        this.changeCallback = accepted -> {
            addon.getConfig().addProperty(attribute, accepted);
            addon.loadConfig();

            if (callback != null) callback.accept(accepted);
        };
    }

    /**
     * Sets the minimal value of the slider
     *
     * @param minValue The minimal value of the slider
     * @return this slider
     */
    public BetterSliderElement minValue(float minValue) {
        this.minValue = minValue;

        if (this.currentValue < this.minValue) this.currentValue = this.minValue;
        return this;
    }

    /**
     * Sets the maximal value of the slider
     *
     * @param maxValue The maximal value of the slider
     * @return this slider
     */
    public BetterSliderElement maxValue(float maxValue) {
        this.maxValue = maxValue;

        if (this.currentValue > this.maxValue) this.currentValue = this.maxValue;
        return this;
    }

    /**
     * Sets a range of the slider
     *
     * @param min The minimal value of the slider
     * @param max The maximal value of the slider
     * @return this slider
     */
    public BetterSliderElement range(float min, float max) {
        this.minValue(min);
        this.maxValue(max);
        return this;
    }

    /**
     * Sets the steps of the slider
     *
     * @param steps The steps of the slider
     * @return this slider
     */
    public BetterSliderElement steps(float steps) {
        this.steps = steps;
        return this;
    }

    /**
     * Add a callback to the slider
     *
     * @param callback The new callback
     * @return this slider
     */
    public BetterSliderElement addCallback(Consumer<Float> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Sets the current value of the slider
     *
     * @param currentValue The new current value
     */
    public void setCurrentValue(Float currentValue) {
        this.currentValue = currentValue;
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        DrawUtils draw = LabyMod.getInstance().getDrawUtils();

        int width = this.getObjectWidth();

        if (this.displayName != null) {
            draw.drawRectangle(x - 1, y, x, maxY, ModColor.toRGB(120, 120, 120, 120));
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(buttonTextures);
        GlStateManager.color(1.0F, 1.0F, 1.0F);

        double sliderWidth = width - 8;
        double minSliderPos = (double) maxX - (double) width;
        double totalValueDiff = this.maxValue - this.minValue;
        double currentValue = this.currentValue;
        double pos = minSliderPos + sliderWidth / totalValueDiff * (currentValue - this.minValue);

        draw.drawTexturedModalRect(minSliderPos, y + 1, 0.0D, 46.0D,
                (double) width / 2.0D, 20.0D);
        draw.drawTexturedModalRect(minSliderPos + (double) width / 2.0D, y + 1,
                200.0D - (double) width / 2.0D, 46.0D, (double) width / 2.0D, 20.0D);

        this.hovered = mouseX > x && mouseX < maxX && mouseY > y + 1 && mouseY < maxY;

        draw.drawTexturedModalRect(pos, y + 1, 0.0D, 66.0D, 4.0D, 20.0D);
        draw.drawTexturedModalRect(pos + 4.0D, y + 1, 196.0D, 66.0D, 4.0D, 20.0D);

        if (!this.isMouseOver()) {
            this.mouseRelease(mouseX, mouseY, 0);
        } else {
            float mouseToMinSlider = (float) (mouseX - minSliderPos);
            float finalValue = (float) (this.minValue + totalValueDiff / sliderWidth * (mouseToMinSlider - 1.0F));
            if (this.dragging) {
                this.dragValue = finalValue;
                this.mouseClickMove(mouseX, mouseY, 0);
            }
        }

        draw.drawCenteredString("" + this.currentValue, minSliderPos + (double) width / 2.0D, y + 7);
    }

    @Override
    public void unfocus(int mouseX, int mouseY, int mouseButton) {
        super.unfocus(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.hovered) {
            this.dragging = true;
        }

    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int mouseButton) {
        super.mouseRelease(mouseX, mouseY, mouseButton);
        if (this.dragging) {
            this.dragging = false;

            this.currentValue = Math.round((this.dragValue / this.steps) * this.steps * 100.0F) / 100.0F;
            if (this.currentValue > this.maxValue) {
                this.currentValue = this.maxValue;
            }

            if (this.currentValue < this.minValue) {
                this.currentValue = this.minValue;
            }

            this.changeCallback.accept(this.currentValue);
        }

    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
        super.mouseClickMove(mouseX, mouseY, mouseButton);
        if (this.dragging) {
            this.currentValue = Math.round(((this.dragValue / this.steps) * this.steps) * 100.0F) / 100.0F;
            if (this.currentValue > this.maxValue) {
                this.currentValue = this.maxValue;
            }

            if (this.currentValue < this.minValue) {
                this.currentValue = this.minValue;
            }

            this.changeCallback.accept(this.currentValue);
        }

    }

    @Override
    public int getObjectWidth() {
        return 50;
    }
}
