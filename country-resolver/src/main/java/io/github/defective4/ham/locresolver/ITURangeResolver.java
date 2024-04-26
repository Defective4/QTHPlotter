package io.github.defective4.ham.locresolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ITURangeResolver {
    private static final class CharacterRange {
        private final char lower, upper;

        private CharacterRange(char lower, char upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public char getLower() {
            return lower;
        }

        public char getUpper() {
            return upper;
        }
    }

    private ITURangeResolver() {
    }

    public static Collection<String> resolve(String lower, String upper) {
        if (lower.length() != upper.length() || lower.isEmpty())
            throw new IllegalArgumentException(lower + "-" + upper);
        CharacterRange first = new CharacterRange(lower.charAt(0), upper.charAt(0));
        CharacterRange second = lower.length() > 1 ? new CharacterRange(lower.charAt(1), upper.charAt(1)) : null;
        CharacterRange third = lower.length() > 2 ? new CharacterRange(lower.charAt(2), upper.charAt(2)) : null;
        List<String> signs = new ArrayList<>();
        for (char i = first.lower; i <= first.upper; i++) {
            if (second != null) {
                for (char j = second.lower; j <= second.upper; j++) {
                    if (third != null) {
                        for (char k = third.lower; k <= third.upper; k++) {
                            signs.add(i + "" + j + k);
                        }
                    } else {
                        signs.add(i + "" + j);
                    }
                }
            } else {
                signs.add("" + i);
            }
        }
        return Collections.unmodifiableCollection(signs);
    }
}
