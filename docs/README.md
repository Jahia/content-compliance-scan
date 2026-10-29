# Documentation index

Read in the order that matches why you are here.

## If you maintain the list of non-authorised terms (Legal, compliance)

- **[`glossary-guide.md`](glossary-guide.md)**: creating a glossary per language, writing terms, how
  matching works (case, accents, phrases, word starts), and what editors then see.

## If you edit pages or content

- **[`page-scan-guide.md`](page-scan-guide.md)**: the **Compliance scan** button of Page Builder, the
  three results, how to read an issue and fix it.
- **[`dashboard-guide.md`](dashboard-guide.md)**: the site-wide report under **Additional >
  Compliance**, its tables, and the CSV and JSON exports.

## If you want to know how it works, or change the code

1. **[`../.agents/README.md`](../.agents/README.md)** first if you are an agent: invariants, security
   decisions, traps already hit once.
2. **[`architecture.md`](architecture.md)**: what each scan reads and why, the matching rules, the
   GraphQL API, permissions, limits, and the known gaps.
3. **[`publication-blocking.md`](publication-blocking.md)**: why the module does not block
   publication yet, what Jahia offers to do it, and the design when it is wanted.

Build, deploy and compatibility are in the root [`README.md`](../README.md).
