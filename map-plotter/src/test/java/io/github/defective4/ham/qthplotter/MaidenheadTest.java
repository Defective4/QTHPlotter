package io.github.defective4.ham.qthplotter;

import org.junit.Assert;
import org.junit.Test;

public final class MaidenheadTest {

    private static final String[] INVALID_LOCATORS = {"JN08m", "RR73", "R-31", "lN05", "2137", "LOOP", "AAAA"};

    @Test
    public void isLocatorTest() {
        /*
        Maidenhead (QTH) locators have to be 4 characters long (in this case).
        They start with two uppercase characters from 'A' to 'R' followed with two digits.
        */
        String[] OK_LOCATORS = {"LN06", "JN08", "LN05", "JO20", "JO21", "IO91", "AA00"};

        for (String loc : OK_LOCATORS)
            Assert.assertTrue(Maidenhead.isLocator(loc));

        for (String loc : INVALID_LOCATORS)
            Assert.assertFalse(Maidenhead.isLocator(loc));
    }

    @Test
    public void decodeTest() {
        Assert.assertEquals(Maidenhead.decode("KN10"), new Maidenhead.Locator(40.500f, 23.000f, "KN10"));
        Assert.assertEquals(Maidenhead.decode("LF85"), new Maidenhead.Locator(-34.500f, 57.000f, "LF85"));
        for (String loc : INVALID_LOCATORS)
            Assert.assertThrows(IllegalArgumentException.class, () -> Maidenhead.decode(loc));
    }
}