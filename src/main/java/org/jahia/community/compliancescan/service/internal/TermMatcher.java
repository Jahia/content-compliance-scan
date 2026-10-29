package org.jahia.community.compliancescan.service.internal;

import org.jahia.community.compliancescan.model.GlossaryTerm;
import org.jahia.community.compliancescan.model.MatchMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Finds glossary terms in plain text, ignoring case and accents. Immutable and thread-safe once built.
 */
public final class TermMatcher {

    /** A term found in a text: offsets are in the original (unfolded) text. */
    public record Match(GlossaryTerm term, int start, int end) {
    }

    private record Compiled(GlossaryTerm term, String folded, Pattern pattern) {
    }

    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final String NOT_AFTER_WORD = "(?<![\\p{L}\\p{N}])";
    private static final String NOT_BEFORE_WORD = "(?![\\p{L}\\p{N}])";
    private static final String WORD_REST = "[\\p{L}\\p{N}]*";

    private final List<Compiled> compiled;

    /**
     * Compiles the terms. Blank terms are ignored.
     *
     * @param terms the glossary terms to look for
     */
    public TermMatcher(Collection<GlossaryTerm> terms) {
        List<Compiled> list = new ArrayList<>();
        for (GlossaryTerm term : terms) {
            if (term.term() == null) {
                continue;
            }
            String folded = WHITESPACE.matcher(FoldedText.foldString(term.term()).trim()).replaceAll(" ");
            if (!folded.isEmpty()) {
                list.add(new Compiled(term, folded, compile(folded, term.mode())));
            }
        }
        this.compiled = List.copyOf(list);
    }

    /** @return {@code true} when no term can match */
    public boolean isEmpty() {
        return compiled.isEmpty();
    }

    /** @return number of terms the matcher looks for */
    public int size() {
        return compiled.size();
    }

    /**
     * Finds every term in a text. When two glossary entries with the same folded term match at the
     * same place, the first one is kept.
     *
     * @param text plain text, may be {@code null}
     * @return the matches, by position in the text
     */
    public List<Match> find(String text) {
        if (text == null || text.isEmpty() || compiled.isEmpty()) {
            return List.of();
        }
        FoldedText folded = FoldedText.of(text);
        List<Match> matches = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Compiled c : compiled) {
            Matcher m = c.pattern().matcher(folded.text());
            while (m.find()) {
                if (m.end() == m.start()) {
                    continue;
                }
                int start = folded.originalStart(m.start());
                int end = folded.originalEnd(m.end());
                if (seen.add(c.folded() + '\u0000' + start + ':' + end)) {
                    matches.add(new Match(c.term(), start, end));
                }
            }
        }
        matches.sort(Comparator.comparingInt(Match::start).thenComparingInt(Match::end));
        return matches;
    }

    static Pattern compile(String foldedTerm, MatchMode mode) {
        String body = WHITESPACE.splitAsStream(foldedTerm)
                .map(Pattern::quote)
                .collect(Collectors.joining("\\s+"));
        String regex = switch (mode) {
            case WORD -> NOT_AFTER_WORD + body + NOT_BEFORE_WORD;
            case PREFIX -> NOT_AFTER_WORD + body + WORD_REST;
            case ANYWHERE -> body;
        };
        return Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
    }
}
