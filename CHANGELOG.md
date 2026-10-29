# Changelog

## 1.0.0 (unreleased)

- Page Builder action **Compliance scan**: scans the page being edited against the site's
  glossaries of non-authorised terms for the language being edited, and lists each use with the
  field, the highlighted sentence, the reason and the suggested wording. Informational: it does not
  block publication.
- Content types `ccs:glossary` (one per language, under the site's contents) and
  `ccs:glossaryTerm` (term, match mode, reason, suggested wording), EN and FR labels.
- Scope: the page and its content, content references, the header and footer of the ancestor pages,
  and the content the Page Builder frame shows from elsewhere.
- Site dashboard in jContent (Additional > Compliance): the whole site in the selected language, terms
  ranked by use, pages and content items with their issues, Export CSV and Export JSON. GraphQL
  `complianceScan.scanSite`, gated by `jContentAccess` and the module being enabled on the site.
- GraphQL `complianceScan.scanPage` and the exported `ComplianceScanService` for a future
  publication guard.
