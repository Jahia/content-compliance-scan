package org.jahia.community.compliancescan.service.internal;

import java.text.Normalizer;
import java.util.Arrays;

/**
 * A text folded for comparison - lower case, accents removed, typographic apostrophes and common
 * ligatures replaced - that keeps, for every folded character, the range it came from in the
 * original text. A match found in the folded text is reported on the original text.
 */
final class FoldedText {

    private final String original;
    private final String text;
    private final int[] starts;
    private final int[] ends;

    private FoldedText(String original, String text, int[] starts, int[] ends) {
        this.original = original;
        this.text = text;
        this.starts = starts;
        this.ends = ends;
    }

    /**
     * Folds a text.
     *
     * @param original the text as written, never {@code null}
     * @return the folded text with its offset map
     */
    static FoldedText of(String original) {
        StringBuilder out = new StringBuilder(original.length());
        int[] starts = new int[original.length() + 8];
        int[] ends = new int[original.length() + 8];
        int i = 0;
        while (i < original.length()) {
            int cp = original.codePointAt(i);
            int next = i + Character.charCount(cp);
            String folded = fold(cp);
            for (int k = 0; k < folded.length(); k++) {
                int at = out.length();
                if (at >= starts.length) {
                    starts = Arrays.copyOf(starts, starts.length * 2);
                    ends = Arrays.copyOf(ends, ends.length * 2);
                }
                starts[at] = i;
                ends[at] = next;
                out.append(folded.charAt(k));
            }
            i = next;
        }
        return new FoldedText(original, out.toString(), starts, ends);
    }

    /**
     * Folds a text without keeping the offset map, for glossary terms.
     *
     * @param original the text as written
     * @return the folded text
     */
    static String foldString(String original) {
        return of(original).text();
    }

    String text() {
        return text;
    }

    /** Offset in the original text of the folded character at {@code foldedIndex}. */
    int originalStart(int foldedIndex) {
        return starts[foldedIndex];
    }

    /**
     * End offset (exclusive) in the original text of a folded range ending at {@code foldedEnd}
     * (exclusive). Combining marks that follow in the original belong to the last letter: they fold
     * to nothing, so they are added here ("risque" + U+0301 is reported whole).
     */
    int originalEnd(int foldedEnd) {
        int end = ends[foldedEnd - 1];
        while (end < original.length() && Character.getType(original.codePointAt(end)) == Character.NON_SPACING_MARK) {
            end += Character.charCount(original.codePointAt(end));
        }
        return end;
    }

    private static String fold(int cp) {
        switch (cp) {
            case '‘', '’', 'ʼ', '′', '`' -> {
                return "'";
            }
            case 'œ', 'Œ' -> {
                return "oe";
            }
            case 'æ', 'Æ' -> {
                return "ae";
            }
            case 'ß' -> {
                return "ss";
            }
            default -> {
                String decomposed = Normalizer.normalize(new String(Character.toChars(cp)), Normalizer.Form.NFD);
                StringBuilder sb = new StringBuilder(decomposed.length());
                decomposed.codePoints()
                        .filter(c -> Character.getType(c) != Character.NON_SPACING_MARK)
                        .forEach(c -> sb.appendCodePoint(Character.toLowerCase(c)));
                return sb.toString();
            }
        }
    }
}
