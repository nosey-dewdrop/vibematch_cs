package ui;

import java.awt.Color;
import java.awt.Font;

/*
 * Theme holds all the colors and fonts for the app in one place.
 * If we want to change how the app looks we change it here, not in 30 panels.
 * The palette is the lilac / pastel one from our mockups.
 */
public class Theme {

    // ---- lilac scale (light to dark) ----
    public static final Color LILAC_50  = new Color(0xF6, 0xF2, 0xFD);
    public static final Color LILAC_100 = new Color(0xEF, 0xE8, 0xFB);
    public static final Color LILAC_200 = new Color(0xE2, 0xD5, 0xF6);
    public static final Color LILAC_300 = new Color(0xCF, 0xB9, 0xEE);
    public static final Color LILAC_400 = new Color(0xB8, 0xA4, 0xE3);
    public static final Color LILAC_500 = new Color(0x9C, 0x82, 0xD6);
    public static final Color LILAC_600 = new Color(0x8B, 0x6F, 0xC7);
    public static final Color LILAC_700 = new Color(0x6F, 0x55, 0xA8);

    // ---- text ----
    public static final Color INK      = new Color(0x3A, 0x30, 0x50); // main text
    public static final Color INK_SOFT = new Color(0x6B, 0x62, 0x80); // secondary / muted

    // ---- pastel accents (used on community cards etc) ----
    public static final Color BLUSH  = new Color(0xFF, 0xD6, 0xE0);
    public static final Color PEACH  = new Color(0xFF, 0xE2, 0xCF);
    public static final Color MINT   = new Color(0xD6, 0xF0, 0xE4);
    public static final Color BUTTER = new Color(0xFD, 0xF2, 0xC9);
    public static final Color SKY    = new Color(0xD7, 0xE8, 0xFB);

    public static final Color CREAM = new Color(0xFF, 0xFD, 0xFB);
    public static final Color WHITE = Color.WHITE;

    // background of most screens
    public static final Color BG   = LILAC_50;
    public static final Color CARD = WHITE;

    // primary action color (buttons)
    public static final Color PRIMARY = LILAC_600;

    /*
     * Fonts. We use a serif for the big titles (gives it that warm feel) and a
     * normal sans for everything else. Georgia is on basicly every machine so
     * we fall back nicely if the nicer fonts arent installed.
     */
    private static final String HEAD_FAMILY = "Georgia";
    private static final String BODY_FAMILY = "SansSerif";

    public static Font heading(int size) {
        return new Font(HEAD_FAMILY, Font.BOLD, size);
    }

    public static Font body(int size) {
        return new Font(BODY_FAMILY, Font.PLAIN, size);
    }

    public static Font bodyBold(int size) {
        return new Font(BODY_FAMILY, Font.BOLD, size);
    }

    // don't let anyone make a Theme object, it's just constants
    private Theme() {
    }
}
