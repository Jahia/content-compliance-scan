package org.jahia.community.compliancescan.service.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlainTextTest {

    @Test
    void blockTagsSeparateWordsAndInlineTagsDoNot() {
        assertThat(PlainText.fromHtml("<p>Livraison</p><p><strong>gra</strong>tuite</p><br/>ici"))
                .isEqualTo("Livraison gratuite ici");
    }

    @Test
    void entitiesAreDecodedAndScriptsDropped() {
        assertThat(PlainText.fromHtml("<style>p{color:red}</style>Satisfait&nbsp;ou&nbsp;rembours&eacute; &amp; plus<!-- note --><script>x()</script>"))
                .isEqualTo("Satisfait ou remboursé & plus");
    }

    @Test
    void collapseJoinsLines() {
        assertThat(PlainText.collapse("  one\n\n two\t three ")).isEqualTo("one two three");
    }
}
