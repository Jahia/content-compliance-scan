package org.jahia.community.compliancescan.service.internal;

import org.jahia.community.compliancescan.model.GlossaryTerm;
import org.jahia.community.compliancescan.model.MatchMode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TermMatcherTest {

    private static GlossaryTerm term(String text, MatchMode mode) {
        return new GlossaryTerm(text, mode, null, null, "/t/" + text, "/g");
    }

    private static List<String> found(String text, GlossaryTerm... terms) {
        return new TermMatcher(List.of(terms)).find(text).stream()
                .map(m -> text.substring(m.start(), m.end()))
                .toList();
    }

    @Test
    void wholeWordIgnoresCaseAndAccents() {
        assertThat(found("Livraison GRATUITE et gratuité garanties.", term("gratuité", MatchMode.WORD)))
                .containsExactly("GRATUITE", "gratuité");
        assertThat(found("Livraison GRATUITÉ.", term("gratuite", MatchMode.WORD)))
                .containsExactly("GRATUITÉ");
    }

    @Test
    void wholeWordDoesNotMatchInsideOrAtTheStartOfAnotherWord() {
        assertThat(found("garantie, garantis, sans garanti.", term("garanti", MatchMode.WORD)))
                .containsExactly("garanti");
    }

    @Test
    void prefixFindsTheWholeWord() {
        assertThat(found("Guaranteed results, a guarantee, unguarded.", term("guarant", MatchMode.PREFIX)))
                .containsExactly("Guaranteed", "guarantee");
    }

    @Test
    void anywhereFindsInsideWords() {
        assertThat(found("superfree and free", term("free", MatchMode.ANYWHERE)))
                .containsExactly("free", "free");
    }

    @Test
    void phraseMatchesAcrossAnyWhiteSpace() {
        assertThat(found("Satisfait ou\n  remboursé !", term("satisfait ou remboursé", MatchMode.WORD)))
                .containsExactly("Satisfait ou\n  remboursé");
    }

    @Test
    void typographicApostrophesAndLigaturesFold() {
        assertThat(found("C’est l’ŒUVRE", term("c'est l'oeuvre", MatchMode.WORD)))
                .containsExactly("C’est l’ŒUVRE");
    }

    @Test
    void offsetsPointAtTheOriginalTextAfterDecomposedCharacters() {
        // "é" written as e + combining acute: two chars in the original, one in the folded text.
        String text = "Café risqué ici";
        assertThat(found(text, term("risque", MatchMode.WORD))).containsExactly("risqué");
    }

    @Test
    void regexCharactersInTermsAreLiteral() {
        assertThat(found("Taux 100% (garanti) ou 100x", term("100%", MatchMode.WORD), term("(garanti)", MatchMode.WORD)))
                .containsExactly("100%", "(garanti)");
    }

    @Test
    void duplicateTermsAtTheSamePlaceAreReportedOnce() {
        assertThat(found("gratuit", term("Gratuit", MatchMode.WORD), term("gratuit", MatchMode.WORD)))
                .containsExactly("gratuit");
    }

    @Test
    void theSameTermInTwoGlossariesIsReportedForEach() {
        GlossaryTerm legal = new GlossaryTerm("autonomous", MatchMode.PREFIX, "Outcome promise", null, "/t/a", "/g/legal");
        GlossaryTerm brand = new GlossaryTerm("Autonomous", MatchMode.WORD, "Brand voice", null, "/t/b", "/g/brand");
        assertThat(new TermMatcher(List.of(legal, brand)).find("fully autonomous teams"))
                .extracting(m -> m.term().glossaryPath())
                .containsExactlyInAnyOrder("/g/legal", "/g/brand");
    }

    @Test
    void blankTermsAreIgnored() {
        TermMatcher matcher = new TermMatcher(List.of(term("  ", MatchMode.WORD)));
        assertThat(matcher.isEmpty()).isTrue();
        assertThat(matcher.find("anything")).isEmpty();
    }
}
