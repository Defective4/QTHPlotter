package io.github.defective4.ham.qthplotter;

import java.util.Objects;

public final class Maidenhead {
    public static class Locator {
        private final float latitude, longtitude;

        public Locator(float latitude, float longtitude) {
            this.latitude = latitude;
            this.longtitude = longtitude;
        }

        public float getLatitude() {
            return latitude;
        }

        public float getLongtitude() {
            return longtitude;
        }

        public Locator add(float deltaLatitude, float deltaLongtitude) {
            return new Locator(this.latitude + deltaLatitude, this.longtitude + deltaLongtitude);
        }

        @Override
        public int hashCode() {
            return Objects.hash(latitude, longtitude);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Locator locator = (Locator) o;
            return Float.compare(latitude, locator.latitude) == 0 && Float.compare(longtitude, locator.longtitude) == 0;
        }

        @Override
        public String toString() {
            return "Locator{" + "latitude=" + latitude + ", longtitude=" + longtitude + '}';
        }
    }

    private Maidenhead() {
    }

    public static Locator decode(String locator) {
        if (!isLocator(locator)) throw new IllegalArgumentException("Invalid locator string");
        return new Locator(10 * (locator.charAt(1) - 'A') + Integer.parseInt(locator.substring(3, 4)) - 90 + 0.5f,
                           20 * (locator.charAt(0) - 'A') + 2 * Integer.parseInt(locator.substring(2, 3)) - 180 + 1f);
    }

    public static boolean isLocator(String locatorString) {
        if (locatorString == null || locatorString.length() != 4 || "RR73".equals(locatorString)) return false;
        char[] chars = locatorString.toCharArray();
        char c;
        for (int x = 0; x < 2; x++) {
            c = chars[x];
            if (c < 'A' || c > 'R') return false;
        }

        for (int x = 2; x < 4; x++) {
            c = chars[x];
            if (c < '0' || c > '9') return false;
        }

        return true;
    }
}
