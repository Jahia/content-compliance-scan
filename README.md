# Content Compliance Scan

A Page Builder action that checks every text of the page being edited against the site's
glossaries of non-authorised terms, in the language being edited, and lists each use: the content,
the field, the sentence with the term highlighted, why the term is not authorised and what to write
instead.

This version is informational: the dialog says a page with issues should not be published, it does
not block publication.

## How editors use it

1. Open a page in Page Builder and click **Compliance scan** in the header.
2. The dialog shows **Compliant**, **Not compliant** (with the list of issues) or **Not checked**
   (no glossary for this language).
3. **Edit** opens the content in Content Editor. When the editor closes, the scan runs again.

## Site dashboard

In jContent, **Additional > Compliance** scans the whole site in the language selected in jContent:

- a summary (compliant or not, uses, pages and content items read, glossaries applied);
- **Terms found**: each term with its reason and suggested wording, how often it is used and on how many
  pages and content items;
- **Pages and content items with issues**: one row per page or content item of the content folders
  (articles, news), expanded into its issues (field, highlighted sentence, term, reason). Pages open
  in Page Builder, content items and issues open in Content Editor;
- **Export CSV** (one row per issue, opens in Excel with accents) and **Export JSON** (the whole report).

Each node counts once: a page owns the content stored under it, so the header and footer count for the
home page, and content references count where their target is stored. The dashboard needs
`jContentAccess` on the site, and the module enabled on it; both are checked by the server. A site scan
stops after 20,000 contents or 5,000 issues and says so.

## How Legal maintains the glossaries

In jContent, under **Content Folders**, create a **Compliance glossary** (category *Compliance*) in
any folder of the site's `contents`:

| Field | Meaning |
|---|---|
| Title | Name shown in the report |
| Language | The site language the glossary applies to. Several glossaries may target one language: all apply |
| Description | Who maintains it, what it covers (not shown to visitors) |

Inside the glossary, add one **Non-authorised term** per word or phrase:

| Field | Meaning |
|---|---|
| Term | Word or phrase. Case and accents are ignored; any white space matches any white space |
| Match | *Whole word or phrase* (default), *Word starting with* (`guarant` finds *guaranteed*), *Anywhere* (inside other words too) |
| Reason | Shown to the editor |
| Suggested wording | Shown to the editor |

Glossaries are read from the **default** workspace (no need to publish them) with the editor's own
rights: an editor who cannot read a glossary is not checked against it, so keep glossaries readable
by every editor.

## What the scan reads

For the page and language being edited, in the default workspace:

- the page's own properties and every content stored under it, sub-pages excluded (**Page**);
- the content a content reference of the page points to (**Reused content**);
- the areas of the ancestor pages, up to the home page, that the page has no area of the same name
  for: the header and footer stored on the home page (**Shared content**). Locked chrome is rendered
  in the edit frame without any marker, so the frame cannot name it;
- the content the Page Builder frame shows that is stored elsewhere: listings (**Shared content**).

A text is a `STRING` property edited as text, text area or rich text, with no value constraint
(choicelist and token values are technical). `jcr:title` and `jcr:description` are read; other
`jcr:`/`j:` properties are not. Rich text is read as the text a visitor sees (tags removed, entities
decoded). A scan stops after 5,000 contents or 1,000 issues and says so.

Who may scan: an authenticated user with `jcr:write` on the page, on a site where the module is
enabled. Both are checked by the server (`complianceScan.scanPage`), not only by the button.

## API

```graphql
query {
  complianceScan {
    scanPage(path: "/sites/mySite/home/about", language: "en", uiLanguage: "en",
             renderedPaths: ["/sites/mySite/home/news/main/latest"]) {
      status            # SAFE | ISSUES | NO_GLOSSARY
      scannedContents
      truncated
      glossaries { path title termCount }
      issues { nodePath nodeUuid nodeName nodeType propertyLabel term matchedText before after
               reason alternative origin originPath }
    }
  }
}
```

```graphql
query {
  complianceScan {
    scanSite(siteKey: "mySite", language: "en", uiLanguage: "en") {
      status scannedPages scannedContents truncated
      terms { term reason alternative uses locations }
      locations { path name kind typeLabel issueCount issues { nodePath propertyLabel term before matchedText after } }
    }
  }
}
```

The Java service behind it, `org.jahia.community.compliancescan.service.spi.ComplianceScanService`,
is exported so that a publication guard can call the same scan.

## Build and deploy

Java 17 and Maven 3.8 (`mise.toml`). Node and Yarn are installed by `frontend-maven-plugin`.

```bash
mvn clean install
```

Deploy `target/content-compliance-scan-<version>.jar` with the module manager or the provisioning
API, then enable the module on the site (Site Settings > Modules).

## Compatibility

Jahia 8.2 with jContent 3.7 or later, graphql-dxm-provider 3.5 or later, Java 17.

## Licence

MIT
