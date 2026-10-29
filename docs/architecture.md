# How it works

One OSGi bundle: a CND (`ccs:glossary`, `ccs:glossaryTerm`), a Java service that scans, a GraphQL
extension that exposes it, and a React 18 UI extension with two entry points (the Page Builder
dialog and the jContent dashboard app).

```
Page Builder dialog ──► complianceScan.scanPage ─┐
                                                 ├─► ComplianceScanService ─► JCR (caller's session, default workspace)
jContent dashboard  ──► complianceScan.scanSite ─┘          │
                                                            └─► GlossaryReader: ccs:glossary under /sites/<site>/contents
```

## Packages

| Package | Holds |
|---|---|
| `model` (exported) | Records of the reports: `ComplianceReport`, `SiteComplianceReport`, `ComplianceIssue`, `LocationReport`, `TermUsage`, `Glossary`, `GlossaryTerm`, enums |
| `service.spi` (exported) | `ComplianceScanService`: `scan(page, uiLocale, renderedPaths)` and `scanSite(site, uiLocale)` |
| `choicelist` | `SiteLanguagesInitializer`: `choicelist[complianceSiteLanguages]`, every language of the site (Jahia's `siteLanguage` offers the live-active ones only) |
| `service.internal` | `ComplianceScanServiceImpl` (tree walk), `GlossaryReader`, `TermMatcher` and `FoldedText` (matching), `PlainText` (rich text to text), `Excerpt` |
| `graphql` | `complianceScan { scanPage, scanSite }` and the `Gql*` views; schema names are prefixed (`ComplianceIssue`, `ComplianceScanStatus`...) |

## What a scan reads

Both scans read the **default** workspace in one content language, with the session of the user who
asks. Nothing is written.

**Text properties.** A `STRING` property whose editor is text, text area or rich text, with no value
constraint (choice lists and tokens are technical values) and not hidden. `jcr:title` and
`jcr:description` are read; every other property of the `jcr`, `j`, `jmix`, `jnt`, `mix`, `nt` and
`rep` namespaces is not. Rich text goes through `PlainText.fromHtml`: scripts, styles and comments
dropped, block tags turned into spaces, inline tags removed (so `<b>gra</b>tuit` stays one word),
entities decoded. System children (`j:translation_*`, `j:acl`, vanity URLs) and the glossaries
themselves are skipped.

**Page scan** (`scanPage`), what the page shows:

| Origin | Read |
|---|---|
| `PAGE` | The page and every node under it, sub-pages excluded |
| `REFERENCE` | The target of each `jmix:nodeReference` (`j:node`) met on the way, with its tree |
| `SHARED` | The `jmix:list` children (areas) of each ancestor page up to home for which the page has no child of the same name: the header and footer on home |
| `SHARED` | The `renderedPaths` the dialog collected from the Page Builder frame (`[jahiatype="module"][path]`): listing items, chrome editable on home. Only paths in the page's site, outside the page's tree and not its ancestors, at most 2,000 |

Locked chrome (a `readOnly` absolute area shown on a page other than its owner) renders in the edit
frame without any `jahiatype` marker, so the frame cannot name it; that is why the ancestor areas
are read on the server.

**Site scan** (`scanSite`), where content is stored: every `jnt:page` of the site with its own tree,
then every content item (each non-folder child of the `contents` folders) with its tree. References
are not followed and ancestor areas are not added, so each node counts once. Locations with issues
come pages first in path order, then content items; terms are ranked by uses.

## Matching

`TermMatcher` folds both the glossary term and the text the same way (`FoldedText`): lower case, NFD
with combining marks removed, typographic apostrophes to `'`, `œ`/`æ`/`ß` to `oe`/`ae`/`ss`. It keeps
an offset map, so a match is reported on the original text, combining marks after the last letter
included. Each term becomes one regular expression (quoted, Unicode classes):

| `matchMode` | Pattern around the term |
|---|---|
| `word` | no word character before, none after |
| `prefix` | no word character before, any word characters after |
| `anywhere` | nothing around |

A word character is a letter or digit of a script that separates words with spaces: Han, Hiragana,
Katakana, Thai, Lao, Khmer and Myanmar letters are not, so a whole-word term matches inside a
Japanese or Chinese sentence and a prefix stops at the end of the term there. As with `\b`, the
check before (after) the term applies only when the term's first (last) character is a word
character: 情報 is found in ユーザー情報, where ー is a Common-script letter. Folding leaves kana and
Han as written but composed (removing combining marks would turn ガ into カ; カ + U+3099 is composed
into ガ, U+F900 into U+8C48) and turns fullwidth ASCII into plain ASCII.

White space inside a term matches any run of white space. Two entries of the same glossary with the
same folded term at the same place are reported once; entries of different glossaries are each
reported, so each glossary owner sees their own reason.

`PlainText.fromHtml` strips tags in one forward pass (every search starts where the previous one
ended; an unclosed tag, comment or script drops the rest of the value), so a hostile value stays
linear: the regular expressions it replaces took 23 s on 160 KB of `<`. `Excerpt` keeps about 60 characters on each side, cut at
spaces.

## API

```graphql
query {
  complianceScan {
    scanPage(path: "/sites/mySite/home/about", language: "en", uiLanguage: "en", renderedPaths: []) {
      status scannedContents truncated
      glossaries { path uuid title termCount }
      issues { nodePath nodeUuid nodeName nodeType propertyName propertyLabel term matchedText
               before after reason alternative glossaryPath origin originPath }
    }
    scanSite(siteKey: "mySite", language: "en", uiLanguage: "en") {
      status scannedPages scannedContents truncated
      glossaries { path uuid title termCount }
      terms { term glossaryPath reason alternative uses locations }
      locations { path uuid name kind typeLabel issueCount issues { nodePath propertyLabel term matchedText before after reason } }
    }
  }
}
```

`status` is `SAFE`, `ISSUES` or `NO_GLOSSARY` (no glossary with terms for the language). `uiLanguage`
sets the language of the type and field labels (English by default). Report text is plain text;
clients escape it.

## Permissions and checks

All checks run on the server; the button and the app entry only decide what is drawn.

| Field | Refused when |
|---|---|
| `scanPage` | guest; invalid language; the path is missing, unreadable or not a `jnt:page`; the caller lacks `jcr:write` on the page; the module is not enabled on the site; more than 5 `scanPage` in one request |
| `scanSite` | guest; invalid language or site key; the site is missing or unreadable; the caller lacks `jContentAccess` on the site; the module is not enabled on the site; more than 1 `scanSite` in one request |

"Enabled" means enabled on the site directly or through a module the site enables
(`getInstalledModulesWithAllDependencies`), as the button and jContent check it. The per-request
caps stop one request from running a scan many times through aliases.

The scans never open a system session: what the caller cannot read, glossaries included, is not
scanned. Both are read-only queries, so the CSRF guard has nothing to decide.

## Limits

| Scan | Contents read | Issues listed |
|---|---|---|
| Page | 5,000 | 1,000 |
| Site | 20,000 | 5,000 |

Past a limit the report sets `truncated` and the UI says so. A page or content item that cannot be
read (a `RepositoryException`: a type definition gone, a provider down) is skipped and counted in
`skippedLocations`; in a page scan, a rendered path or ancestor that cannot be read is skipped. Rendered paths taken from the dialog:
2,000. On a site of 86 pages and 2,272 contents the site scan takes about a second.

## Known gaps

- Not read: images (`alt` texts in rich text, image titles), choice-list labels, tags and categories,
  files and their metadata.
- A page scan can list an ancestor's area that the page's template does not display (an area name
  used only on the home page template).
- The site scan does not follow content references to other sites.
- Matching edge cases not handled: halfwidth katakana (ｶﾞｲﾄﾞ is not ガイド), Thai tone marks
  (stripped with other combining marks, so ไม่ also finds ไม), Greek final ς (not folded to σ), and a
  supplementary-plane letter right before a term is not seen by the boundary check.
- The glossary type shows in Page Builder's add-content menu (content folders accept only
  `jmix:droppableContent`); a glossary on a page is never read.
- A translator with `jcr:modifyProperties_<lang>` but no `jcr:write` on the page cannot run the page
  scan.
- The page scan opened from jContent's page list (not Page Builder) has no frame, so listing items
  are not read; the dialog says so.
- Publication is not blocked: [`publication-blocking.md`](publication-blocking.md).
- No Cypress suite yet; unit tests cover matching, rich text (hostile values included), excerpts,
the rendered-path filter and match modes. The permission refusals are verified by hand
(GraphQL probes), not by tests.
