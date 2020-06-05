package org.burgerbude.labymod.addons.pingtag.render;

/**
 * <b>DisplayMode</b> is representing the display modes of the renders
 *
 * @author Robby
 */
public enum DisplayMode {

    ABOVE_HEAD_TEXT("Text above head"),
    ABOVE_HEAD_TEXTURE("Icon above head"),
    ABOVE_HEAD_TEXT_TEXTURE("Icon with Text above head");

    private final String displayName;

    DisplayMode(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name of a display mode
     *
     * @return a translated display name or the fallback
     */
    public String displayName() {
        return displayName;
    }
}
