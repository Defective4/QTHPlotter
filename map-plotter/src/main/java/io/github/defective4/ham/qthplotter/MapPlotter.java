package io.github.defective4.ham.qthplotter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapPlotter {
    public static class Location {
        private final int x, y;

        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "Location{" + "x=" + x + ", y=" + y + '}';
        }
    }

    public enum ColorMode {
        STATIC, COUNTRY, SIGNAL
    }

    public enum LocatorMode {
        SQUARE, SYMBOL
    }

    private static final Color[] RANDOM_COLORS;

    static {
        Color[] cs;
        try {
            List<Color> colors = new ArrayList<>();
            for (Field f : Color.class.getFields())
                if (f.getType() == Color.class) colors.add((Color) f.get(null));
            cs = colors.toArray(new Color[0]);
        } catch (Exception e) {
            cs = new Color[]{Color.RED};
        }
        RANDOM_COLORS = cs;
    }

    private BufferedImage originalMap, plotted, locatorSymbol;
    private ColorMode colorMode = ColorMode.STATIC;
    private Color staticColor = Color.red;
    private LocatorMode locatorMode = LocatorMode.SQUARE;
    private boolean overlayMode, signsMode;

    public MapPlotter() {
    }

    private static Color gradient(Color low, Color high, float level) {
        if (level < 0) level = 0;
        if (level > 1) level = 1;
        return new Color((int) (high.getRed() * level + low.getRed() * (1f - level)),
                         (int) (high.getGreen() * level + low.getGreen() * (1f - level)),
                         (int) (high.getBlue() * level + low.getBlue() * (1f - level)));
    }

    private static BufferedImage renderCallsign(String callsign, Graphics2D reference) {
        StringBuilder country = new StringBuilder();
        for (String ch : callsign.split("")) {
            try {
                Integer.parseInt(ch);
                break;
            } catch (Exception e) {
                country.append(ch);
            }
        }
        if (country.length() == 0) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        reference.setFont(reference.getFont().deriveFont(Font.BOLD).deriveFont(32f));
        BufferedImage img = new BufferedImage(reference.getFontMetrics().stringWidth(country.toString()),
                                              reference.getFontMetrics().getHeight(),
                                              BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = img.createGraphics();
        g2.setFont(reference.getFont());
        g2.setColor(Color.BLACK);
        g2.drawString(country.toString(), 0, img.getHeight());
        return img;
    }

    public boolean isSignsMode() {
        return signsMode;
    }

    public void setSignsMode(boolean signsMode) {
        this.signsMode = signsMode;
    }

    public boolean isOverlayMode() {
        return overlayMode;
    }

    public void setOverlayMode(boolean overlayMode) {
        this.overlayMode = overlayMode;
    }

    public BufferedImage getLocatorSymbol() {
        return locatorSymbol;
    }

    public void setLocatorSymbol(BufferedImage locatorSymbol) {
        this.locatorSymbol = locatorSymbol;
    }

    public LocatorMode getLocatorMode() {
        return locatorMode;
    }

    public void setLocatorMode(LocatorMode locatorMode) {
        this.locatorMode = locatorMode;
    }

    public BufferedImage getBaseMap() {
        return originalMap;
    }

    public void setBaseMap(BufferedImage originalMap) {
        this.originalMap = originalMap;
        this.plotted = new BufferedImage(originalMap.getWidth(), originalMap.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public Color getStaticColor() {
        return staticColor;
    }

    public void setStaticColor(Color staticColor) {
        this.staticColor = staticColor;
    }

    public void plot(List<StationLocator> stations) {
        if (overlayMode) setBaseMap(originalMap);
        Graphics2D g2 = plotted.createGraphics();
        if (!overlayMode) g2.drawImage(originalMap, 0, 0, null);
        for (StationLocator station : stations) {
            Maidenhead.Locator location = station.getLocator();
            Location from = mercatorProjection(location);
            Location to = mercatorProjection(location.add(1, 2));
            int diffX = to.x - from.x;
            int diffY = from.y - to.y;

            int borderWidth = diffY / 20;
            Color baseColor;
            switch (colorMode) {
                case SIGNAL: {
                    baseColor = gradient(Color.red, Color.green, (float) (station.getSignal() + 20) / 30);
                    break;
                }
                case COUNTRY: {
                    String sign = station.getCallsign();
                    if (sign.length() < 2) {
                        baseColor = Color.RED;
                        break;
                    }
                    Random rand = new Random(sign.charAt(0) + sign.charAt(1));
                    baseColor = RANDOM_COLORS[rand.nextInt(RANDOM_COLORS.length)];
                    break;
                }
                default:
                case STATIC: {
                    baseColor = staticColor;
                    break;
                }
            }

            switch (locatorMode) {
                default:
                case SQUARE: {
                    g2.setStroke(new BasicStroke(borderWidth));
                    g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 255 / 2));
                    g2.fillRect(from.x - diffX / 2, from.y - diffY / 2, to.x - from.x, from.y - to.y);
                    g2.setColor(baseColor);
                    g2.drawRect(from.x - diffX / 2, from.y - diffY / 2, to.x - from.x, from.y - to.y);
                    break;
                }
                case SYMBOL: {
                    BufferedImage newLocator = new BufferedImage(locatorSymbol.getWidth(),
                                                                 locatorSymbol.getHeight(),
                                                                 locatorSymbol.getType());
                    newLocator.createGraphics().drawImage(locatorSymbol, 0, 0, null);
                    for (int x = 0; x < newLocator.getWidth(); x++)
                        for (int y = 0; y < newLocator.getHeight(); y++) {
                            if (newLocator.getRGB(x, y) == Color.black.getRGB()) {
                                newLocator.setRGB(x, y, baseColor.getRGB());
                            } else {
                                newLocator.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
                            }
                        }
                    int x = from.x;
                    int y = from.y;
                    g2.drawImage(newLocator, x - newLocator.getWidth() / 2, y - newLocator.getHeight(), null);
                    if (signsMode) {
                        BufferedImage rendered = renderCallsign(station.getCallsign(), g2);
                        g2.drawImage(rendered,
                                     x - newLocator.getWidth() / 2 + newLocator.getWidth() / 4,
                                     (int) (y - newLocator.getHeight() + newLocator.getHeight() / 3),
                                     newLocator.getWidth() / 2,
                                     newLocator.getHeight() / 2,
                                     null);
                    }
                    break;
                }
            }
        }
    }

    public Location mercatorProjection(
            Maidenhead.Locator locator
    ) {
        float lon = locator.getLongtitude();
        float lat = locator.getLatitude();
        double mX = (lon + 180) / 360;
        double sin = Math.sin(lat * Math.PI / 180);
        double mY = 0.5 - Math.log((1d + sin) / (1d - sin)) / (4d * Math.PI);

        return new Location((int) (mX * (double) originalMap.getWidth()),
                            (int) (mY * (double) originalMap.getHeight()));
    }

    public BufferedImage getPlottedMap() {
        return plotted;
    }
}
