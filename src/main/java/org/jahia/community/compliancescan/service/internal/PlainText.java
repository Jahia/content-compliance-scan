package org.jahia.community.compliancescan.service.internal;

import org.apache.commons.text.StringEscapeUtils;

import java.util.regex.Pattern;

/**
 * Turns property values into the text a visitor reads: tags removed from rich text, entities
 * decoded, white space collapsed.
 */
final class PlainText {

    private static final Pattern SCRIPT_OR_STYLE = Pattern.compile("(?is)<(script|style)\\b.*?</\\1\\s*>");
    private static final Pattern COMMENT = Pattern.compile("(?s)<!--.*?-->");
    // Block-level tags separate words; inline tags (strong, em, span, a) do not: <b>gra</b>tuit is one word.
    private static final Pattern BLOCK_TAG = Pattern.compile(
            "(?i)</?(p|div|br|hr|li|ul|ol|dl|dt|dd|h[1-6]|table|thead|tbody|tfoot|tr|td|th|caption|section|article"
                    + "|header|footer|nav|aside|main|blockquote|figure|figcaption|pre|address|details|summary)\\b[^>]*>");
    private static final Pattern TAG = Pattern.compile("(?s)<[^>]*>");
    private static final Pattern SPACES = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private PlainText() {
    }

    /**
     * @param html a rich text value
     * @return the visible text, on one line
     */
    static String fromHtml(String html) {
        String s = SCRIPT_OR_STYLE.matcher(html).replaceAll(" ");
        s = COMMENT.matcher(s).replaceAll(" ");
        s = BLOCK_TAG.matcher(s).replaceAll(" ");
        s = TAG.matcher(s).replaceAll("");
        return collapse(StringEscapeUtils.unescapeHtml4(s));
    }

    /**
     * @param text a plain text value (small text, text area)
     * @return the text on one line
     */
    static String collapse(String text) {
        return SPACES.matcher(text).replaceAll(" ").trim();
    }
}
