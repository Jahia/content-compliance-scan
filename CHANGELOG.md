# Changelog

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
- GraphQL `complianceScan { scanPage, scanSite }` and the exported `ComplianceScanService`.
- Documentation: `docs/` guides for glossary maintainers, editors and dashboard users, the
  architecture, and the publication-blocking analysis.

### Security

- Both scans run with the caller's own session (default workspace, read-only, no system session).
  `scanPage` needs `jcr:write` on the page, `scanSite` needs `jContentAccess` on the site, and both
  refuse guests and sites where the module is not enabled; paths sent by the browser are restricted
  to the page's site.

### Fixed (before release)

- The site dashboard could not scroll in jContent's Additional panel: its wrapper now fills the
  panel, so the report scrolls under a fixed header.
