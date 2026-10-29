# Content Compliance Scan - agent harness

Track 2 OSGi UI extension with a Java service: Maven bundle, React 18, Webpack + Module Federation,
`@jahia/ui-extender`, plus a GraphQL extension and a CND. Generic Jahia rules live in AIStartupKit
(`CLAUDE.md`, skills `jahia-osgi-ui-extension`, `jahia-java-security`, `jahia-java-jcr`); this file
keeps what is specific to this module.

## Invariants

- React **18** only (jContent singleton). Moonstone **2.20.3**, the version jContent 3.7.1 and 3.8.0
  run: no API newer than that.
- Build with Maven **3.8** and Java 17 (`mise.toml`), always `clean install`. Never `webpack --watch`.
- Bundle symbolic name and module id `content-compliance-scan`: the action's
  `requireModuleInstalledOnSite`, the server's `GqlComplianceScan.MODULE_ID` and the locale
  namespace all use it. Rename all or none.
- Every UI string in `src/main/resources/javascript/locales/{en,fr}.json` (same keys in both; plural
  keys as `key`/`key_plural` and `key_one`/`key_other`). Every CND label and tooltip in
  `src/main/resources/resources/content-compliance-scan{,_fr}.properties`.
- `ccs:glossary` and `ccs:glossaryTerm` are released types once 1.0.0 ships: a change goes through
  `/jahia-dev-change-cnd`. `matchMode` values (`word`, `prefix`, `anywhere`) are read by
  `MatchMode.fromValue`: keep them in sync.

## Security decisions

- `complianceScan.scanPage` is read-only (Apollo GET/POST query, no CSRF decision needed). It
  refuses guests, a path that is not a `jnt:page`, a page without `jcr:write` for the caller
  (`GqlComplianceScan.REQUIRED_PERMISSION`), and a site that does not enable the module.
- Everything is read with the caller's own session (default workspace, content locale). No system
  session, glossaries included: a glossary the editor cannot read does not apply to them.
- `renderedPaths` comes from the browser: only paths inside the page's site, outside the page's own
  tree and not an ancestor of the page are read, at most 2,000, through the caller's session.
- Report text is plain text; the dialog renders it as React text (no `innerHTML`).

## Scope of a scan, and why

- The page tree, sub-pages excluded, plus `jmix:nodeReference` targets.
- **Ancestor-page areas** (header/footer on home): locked chrome (`readOnly` absolute areas on a page
  other than their owner) renders in the edit frame with **no** `jahiatype` marker, so the frame
  cannot report it (verified on a classic-templates site, jContent 3.7.1). The server takes every
  `jmix:list` child of each ancestor page up to home whose name the page does not have. Known
  limit: an ancestor's area that the page's template does not render (a home-only area name) is
  reported too.
- **Frame paths**: `[jahiatype="module"][path]` of the active Page Builder frame (listings,
  editable chrome on home). Collected only when the frame's `mainmodule` is the scanned page.

## Verify

- `mvn clean install` runs the unit tests (`TermMatcher`, `PlainText`, `Excerpt`).
- On a local Jahia: deploy, enable on a site, create a glossary, then
  `complianceScan { scanPage(path, language) { status issues { term nodePath origin } } }` and the
  dialog in Page Builder (`/jahia/jcontent/<site>/<lang>/pages/<path>`).

## Backlog

- Block publication: a publication validation (workflow step or `jmix:publication` listener) that
  calls `ComplianceScanService`; the scope there is what the publication sends (page tree), not the
  chrome.
- Cypress suite: happy path, guest refused, site without the module, no glossary.
- Image `alt` texts inside rich text, and image node titles, are not read.
