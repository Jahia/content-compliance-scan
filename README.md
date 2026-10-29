# Content Compliance Scan

> [!IMPORTANT]
> **Deprecated: replaced by [Content Governance](https://github.com/Jahia/content-governance).** Content Compliance Scan is now part of Content Governance
> 1.0.0, which brings its page scan and site compliance report together with the other content checks in one drawer, switched on per
> site. This repository receives no further releases and will be archived.
>
> To move: uninstall Content Compliance Scan, then install Content Governance and enable it on your sites; compliance glossaries carry over as they are.
> Details: [moving from the three modules](https://github.com/Jahia/content-governance#moving-from-the-three-modules).

Checks site content against glossaries of non-authorised terms, one glossary list per language,
maintained by Legal in jContent. Two entry points:

- **Compliance scan** in the Page Builder header: the page being edited, in the language being
  edited, with every use listed (content, field, highlighted sentence, reason, suggested wording)
  and an Edit button per content.
- **Additional > Compliance** in jContent: the whole site in one language, terms ranked by use,
  pages and content items with their issues, **Export CSV** and **Export JSON**.

Latest release: [1.0.1](https://github.com/Jahia/content-compliance-scan/releases/tag/content-compliance-scan-1.0.1)
([changelog](CHANGELOG.md)).

This version is informational: a page with issues is flagged "do not publish yet", publication is
not blocked ([why, and how it could be](docs/publication-blocking.md)).

## Documentation

| For | Read |
|---|---|
| Legal, compliance: maintaining the terms | [`docs/glossary-guide.md`](docs/glossary-guide.md) |
| Editors: the Page Builder scan | [`docs/page-scan-guide.md`](docs/page-scan-guide.md) |
| Editors, site owners: the site dashboard and exports | [`docs/dashboard-guide.md`](docs/dashboard-guide.md) |
| Developers: scope, matching, API, permissions, limits, known gaps | [`docs/architecture.md`](docs/architecture.md) |
| Blocking publication (not implemented) | [`docs/publication-blocking.md`](docs/publication-blocking.md) |
| Agents changing the code | [`.agents/README.md`](.agents/README.md) |

## Quick start

1. Install the jar of the [latest release](https://github.com/Jahia/content-compliance-scan/releases/latest):
   *Administration > Server > Modules and Extensions > Modules*, *Installed modules*, *Upload module
   from file*. Then click *Content Compliance Scan* in the list and, under *Usage in sites*, switch the
   site to *Enabled*.
2. In jContent, **Content Folders**: under the site's `contents`, create a **Compliance glossary**
   (category *Compliance*), pick its language, then add **Non-authorised terms** (term, match,
   reason, suggested wording).
3. Open a page in Page Builder and click **Compliance scan**, or open **Additional > Compliance**.

## Content types

| Type | Properties |
|---|---|
| `ccs:glossary` (`mix:title`, `jmix:list`, category `ccsmix:complianceContent`) | `language` (site languages), `description`; children `ccs:glossaryTerm` |
| `ccs:glossaryTerm` | `term` (primary item, so jContent lists the term), `matchMode` (`word`, `prefix`, `anywhere`), `reason`, `alternative` |

Labels and tooltips in English and French. Icons in `src/main/resources/icons/`, rendered from Lucide
glyphs listed in `icons.json` (AIStartupKit `.agents/tools/content-type-icons`): `book-a`
(glossary), `whole-word` (term), `shield-check` (the Compliance category).

## Permissions

| Entry point | Needs | Checked |
|---|---|---|
| Page scan (`complianceScan.scanPage`) | `jcr:write` on the page, module enabled on the site (directly or through a module the site enables) | by the server; the button uses the same checks to show itself |
| Site dashboard (`complianceScan.scanSite`) | `jContentAccess` on the site, module enabled on the site (same rule) | by the server; jContent uses the same checks to list the app |

Scans read with the caller's own rights, in the default workspace, and never write. Details:
[`docs/architecture.md`](docs/architecture.md).

## Build and deploy

Java 17 and Maven 3.8 (`mise.toml`; under Maven 3.9 the Jahia plugin writes a wrong
`Import-Package`). Node 22 and Yarn are installed by `frontend-maven-plugin`.

```bash
mvn clean install
```

The jar must hold `javascript/apps/remoteEntry.js` and `javascript/apps/package.json`. Deploy
`target/content-compliance-scan-<version>.jar` with the module manager or the provisioning API. A
version without `-SNAPSHOT` that is already installed is skipped (`already installed, skip` in the
log): add `"forceUpdate":true` to replace it.

```bash
curl -u root:<password> http://localhost:8080/modules/api/provisioning \
  -F 'script=[{"installOrUpgradeBundle":"content-compliance-scan-<version>.jar","start":true}];type=application/json' \
  -F file=@target/content-compliance-scan-<version>.jar
```

`mvn clean install` runs the unit tests (term matching, rich text to text, excerpts). GitHub Actions
(`.github/workflows/build.yml`) runs the same build on every pull request and push to `main`, and
builds the release jar from the tag: `gh workflow run build.yml --ref content-compliance-scan-X.Y.Z`.

## Compatibility

Jahia 8.2 (tested on 8.2.3.2) with jContent 3.7 or later, enforced by `Jahia-Depends: jcontent=[3.7,4)` (UI tested on 3.7.1, API also on 3.8.0),
graphql-dxm-provider 3.5 or later, Java 17. React 18 and Moonstone 2.20 from the jContent host.

## Licence

MIT
