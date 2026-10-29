# Content Compliance Scan - agent harness

**Deprecated (2026-10-29):** this module is replaced by Content Governance (https://github.com/Jahia/content-governance). Do not develop here; work goes to content-governance.

Track 2 OSGi UI extension with a Java service: Maven bundle, React 18, Webpack + Module Federation,
`@jahia/ui-extender`, plus a GraphQL extension and a CND. Generic Jahia rules live in AIStartupKit
(`CLAUDE.md`, skills `jahia-osgi-ui-extension`, `jahia-java-security`, `jahia-java-jcr`); this file
keeps what is specific to this module. User and developer documentation is in `../docs/`
(`architecture.md` for scope, matching, API and limits); keep it in step with every change.

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
- Every node type and category mixin has an `icons.json` entry and a PNG in
  `src/main/resources/icons/`, rendered with AIStartupKit's `.agents/tools/content-type-icons`
  (rule 32). A same-version jar redeploy is skipped by Jahia: use `forceUpdate` when testing a rebuilt
  release version.
- The glossary language list is `choicelist[complianceSiteLanguages]` (`choicelist/SiteLanguagesInitializer`,
  a DS `ModuleChoiceListInitializer`): every site language, since inactive-live ones are when Legal
  needs a glossary. Matching treats Han, kana, Thai, Lao, Khmer and Myanmar letters as non-word
  characters (`TermMatcher.WORD_CHAR`) and keeps kana and Han unfolded (`FoldedText`).
- `ccs:glossary` and `ccs:glossaryTerm` are released types once 1.0.0 ships: a change goes through
  `/jahia-dev-change-cnd`. `matchMode` values (`word`, `prefix`, `anywhere`) are read by
  `MatchMode.fromValue`: keep them in sync.

## Security decisions

- `complianceScan.scanPage` is read-only (Apollo GET/POST query, no CSRF decision needed). It
  refuses guests, a path that is not a `jnt:page`, a page without `jcr:write` for the caller
  (`GqlComplianceScan.REQUIRED_PERMISSION`), and a site that does not enable the module.
- Everything is read with the caller's own session (default workspace, content locale). No system
  session, glossaries included: a glossary the editor cannot read does not apply to them.
- `complianceScan.scanSite` refuses guests, a site key outside `[A-Za-z0-9_-]`, a site without
  `jContentAccess` for the caller (`GqlComplianceScan.SITE_PERMISSION`) and a site that does not
  enable the module. It reads with the caller's session too.
- `renderedPaths` comes from the browser: only paths inside the page's site, outside the page's own
  tree and not an ancestor of the page are read, at most 2,000, through the caller's session.
- Report text is plain text; the dialog renders it as React text (no `innerHTML`).
- Cost: `PlainText.fromHtml` is a single forward pass (no regex per tag: 23 s on hostile input
  before), one request runs at most 5 `scanPage` and 1 `scanSite` (`GqlComplianceScan.countScan`), and
  the walks stop at the node and issue caps. Keep these when changing the scan.

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

## Site dashboard

- jContent app `contentComplianceDashboard` (target `jcontent:90`, shown under Additional), remounted
  per site and language. Exports are built in the browser (`ComplianceDashboard/exportReport.js`), per
  the AIStartupKit rule that every global dashboard exports CSV and JSON.
- `scanSite` counts each node once: every `jnt:page` with its own tree, then every non-folder child of
  the `contents` folders with its tree; no reference following, no ancestor chrome. Locations are
  pages first, in path order; terms most used first.

- The app's root `div` wraps `LayoutContent` for `contain: inline-size`; it must stay a flex column
  that fills the panel (`flex: 1 1 0%; min-height: 0`), or the report cannot scroll (fixed before
  1.0.0; AIStartupKit rule 30).

## Verify

- `mvn clean install` runs the unit tests (`TermMatcher`, `PlainText`, `Excerpt`).
- On a local Jahia: deploy, enable on a site, create a glossary, then
  `complianceScan { scanPage(path, language) { status issues { term nodePath origin } } }` and the
  dialog in Page Builder (`/jahia/jcontent/<site>/<lang>/pages/<path>`).
- Dashboard: `complianceScan { scanSite(siteKey, language) { status scannedPages locations { path issueCount } } }`
  and `/jahia/jcontent/<site>/<lang>/apps/contentComplianceDashboard`: full width at 1600 px
  (`jahia-osgi-ui-extension`, "The page"), `.moonstone-layoutContent` scrolls
  (`scrollHeight > clientHeight`, then a wheel scroll), exports checked by intercepting
  `URL.createObjectURL` (row count, BOM bytes), never by downloading.

## Backlog

- Block publication: parked by the user (2026-10-29) until a customer needs it. Only a publication
  workflow can refuse a publication (listeners run after it, validators are skipped during it), and
  the GraphQL `publish` mutation skips workflows for holders of `publish`. Design and sources:
  `docs/publication-blocking.md`.
- Cypress suite: happy path, guest refused, editor without `jcr:write`, site without the module,
  no glossary (both 1.0.0 reviews asked for it).
- Hide the glossary type from Page Builder's palette.
- Image `alt` texts inside rich text, and image node titles, are not read.
