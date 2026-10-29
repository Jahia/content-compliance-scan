# The site dashboard

The page scan answers for one page. The dashboard answers for the whole site: which terms are used,
how often, and where.

Open it in jContent under **Additional > Compliance**. It scans the site in the language selected in
jContent's language switcher; switch language to scan another one. It needs access to jContent on the
site, and the module enabled on it.

## The summary card

- **Compliant**, **Not compliant** or **Not checked** (no glossary for this language), and the number
  of uses on how many pages and content items.
- How much was read (pages and contents), and the glossaries applied; click one to open it.
- A warning when pages or content items could not be read (a missing content type, an external
  provider down): they are left out of the report and the Jahia log names them.
- A warning when the site is larger than one scan reads (20,000 contents or 5,000 issues): the report
  then covers the first part of the site only.

## Terms found

One row per term and glossary (the glossary is named under the term when several apply): the reason
and suggested wording, the number of uses, and the number of pages
and content items that use it. The most used terms come first: they are where a wording change pays
off most.

## Pages and content items with issues

One row per page, then one per content item of the content folders (articles, news items), with
their number of issues. Expand a row to see each issue: the content and field, the sentence with the
term highlighted, the term and the reason.

- **Open in Page Builder** opens a page, where the page scan gives the full detail.
- **Edit** opens a content item, or the content holding one issue, in Content Editor.
- Ten rows per page by default; the pagination is under the table.

Each content counts once, where it is stored: the header and footer count for the home page, not for
every page that shows them, and reused content counts where the original is stored. That is why the
total can be lower than the sum of the page scans.

## Exports

- **Export CSV**: one row per issue, with the page or content item, the content, the field, the term,
  the matched text, the sentence, the reason, the suggested wording, the glossary name and its path. It opens in
  Excel with accents intact.
- **Export JSON**: the whole report as the dashboard received it, with the time of the scan.

File names carry the site, the language and the date: `compliance-<site>-<language>-<date>.csv`.

**Scan again** reruns the scan; the dashboard does not refresh by itself.
