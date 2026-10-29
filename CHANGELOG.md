# Changelog

## 1.0.1 (2026-10-29)

### Fixed

- Japanese and Chinese: a *Whole word* or *Word starting with* term never matched inside a sentence,
  since these languages write words without spaces. Letters of Han, Hiragana, Katakana, Thai, Lao,
  Khmer and Myanmar no longer extend a word, so 安全 is found in 安全なロック機能 and a prefix stops at
  the end of the term. Kana keep their voiced marks (ガ is not カ), and fullwidth Latin letters and
  digits match their plain forms.
- A glossary's *Language* list offers every language of the site, also those not yet active on the
  live site (Jahia's own site-language list showed the live-active ones only, so a glossary for a
  language being prepared could not be written or edited).

### Changed

- README: the install steps name the module manager's screens (upload, then Usage in sites).

## 1.0.0 (2026-10-29)

### Added

- Page Builder action **Compliance scan**: scans the page being edited against the site's
  glossaries of non-authorised terms for the language being edited, and lists each use with the
  field, the highlighted sentence, the reason and the suggested wording. Edit opens the content in
  Content Editor and the scan runs again when the editor closes. Informational: it does not block
  publication.
- Page scan scope: the page and its content, content references, the header and footer stored on
  the ancestor pages (read on the server, since locked chrome carries no marker in the edit frame),
  and the listing items the Page Builder frame shows.
- Site dashboard in jContent (**Additional > Compliance**): the whole site in the language selected
  in jContent, terms ranked by use, pages and content items with their issues as expandable rows,
  Open in Page Builder and Edit, **Export CSV** (one row per issue, UTF-8 with BOM, formula-safe) and
  **Export JSON** (the whole report with the scan time). Each node counts once, where it is stored.
- Content types `ccs:glossary` (one per language, under the site's contents) and
  `ccs:glossaryTerm` (term, match mode `word`/`prefix`/`anywhere`, reason, suggested wording), EN and
  FR labels and tooltips. Matching ignores case and accents, and phrases match across any white space.
- Several glossaries per language: all apply, each issue names its glossary, and a term two
  glossaries share is reported for each with its own reason.
- Content-type icons (Lucide glyphs) for the glossary, the term and the Compliance category.
- GraphQL `complianceScan { scanPage, scanSite }` and the exported `ComplianceScanService`.
- Documentation: `docs/` guides for glossary maintainers, editors and dashboard users, the
  architecture, and the publication-blocking analysis.

### Security

- Both scans run with the caller's own session (default workspace, read-only, no system session).
  `scanPage` needs `jcr:write` on the page, `scanSite` needs `jContentAccess` on the site, and both
  refuse guests and sites where the module is not enabled (directly or through a dependency); paths
  sent by the browser are restricted to the page's site, and one unreadable path or location is
  skipped instead of failing the scan. One request runs at most 5 page scans and 1 site scan, and
  rich text is read in linear time whatever its markup.
- Requires jContent 3.7 or later (`Jahia-Depends: jcontent=[3.7,4)`).

### Fixed (before release)

- The site dashboard could not scroll in jContent's Additional panel: its wrapper now fills the
  panel, so the report scrolls under a fixed header.
