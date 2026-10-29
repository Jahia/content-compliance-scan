# Glossaries, for the people who maintain the terms

A glossary is the list of words and phrases that must not appear in the content of one language of
the site. Each scan (a page in Page Builder, or the whole site in the dashboard) checks the content of
one language against every glossary of that language.

## Create a glossary

1. In jContent, open **Content Folders** and pick a folder under the site's `contents` (any folder,
   any depth; a dedicated `compliance-glossaries` folder keeps things tidy).
2. **New content > Compliance > Compliance glossary**.
3. Fill in:

| Field | What to enter |
|---|---|
| Title | The name editors see in the reports ("Legal - claims and wording (EN)") |
| Language | The site language the glossary applies to. The list shows the languages of the site |
| Description | Who maintains it and what it covers. Never shown to visitors |

You can have several glossaries for the same language (one from Legal, one from Brand): a scan
applies them all and says which ones it applied.

## Several glossaries for one language

- Every glossary of the language applies; the reports list them under **Glossaries applied**.
- When more than one applies, every issue names its glossary (the page scan, the dashboard and the
  CSV export, which also has a `glossary` column).
- The same term in two glossaries is reported once **per glossary**, each with its own reason and
  suggested wording: Legal and Brand both see their rule. The counts then include both.
- Within one glossary, two entries that find the same text are reported once.

## Add terms

Open the glossary and use **New Non-authorised term**. One entry per word or phrase:

| Field | What to enter |
|---|---|
| Term | The word or phrase, as you would write it |
| Match | How the term is found (below) |
| Reason | Why it is not authorised. Editors read it in the report, so write it for them |
| Suggested wording | What to write instead. Shown next to the reason |

The terms list in jContent shows each entry by its term.

## How matching works

- **Case and accents are ignored.** "gratuité" also finds "Gratuite" and "GRATUITÉ".
  Typographic apostrophes count as straight ones (’ and '), and "œ"/"æ" as "oe"/"ae".
- **Phrases match across any white space**, line breaks and non-breaking spaces included:
  "satisfait ou remboursé" finds "satisfait&nbsp;ou remboursé".
- **Match** decides how much of a word must match:

| Match | Term | Finds | Does not find |
|---|---|---|---|
| Whole word or phrase (default) | `best` | "the best talent" | "bestseller" |
| Word starting with | `guarant` | "guarantee", "guaranteed" | "unguaranteed" |
| Anywhere, inside words too | `free` | "free", "carefree" | |

- **Japanese, Chinese, Thai, Lao, Khmer and Burmese** write words without spaces: there, a term is
  found wherever it is written (安全 in 安全なロック機能), and *Word starting with* matches the term
  itself. Kana keep their voiced marks (ガイド is not カイト), and fullwidth Latin letters count as
  plain ones (ＦＲＥＥ is "free").
- Rich text is read as the visitor reads it: formatting inside a word does not hide it
  (`<b>gra</b>tuit` is "gratuit"), and HTML tags or attributes never match.

## Things to know

- **No need to publish a glossary.** Scans read the version you are editing, so a new term applies
  at the next scan.
- **Editors must be able to read the glossaries.** A scan reads everything with the rights of the
  person who runs it: a glossary someone cannot read does not apply to their scans. Keep glossaries in
  a folder every editor can read.
- **The Language list shows the languages active on the live site.** A language being prepared,
  not yet active in live, cannot be chosen yet.
- **Keep glossaries in the content folders.** The type is also offered when adding content to a
  page in Page Builder (content folders accept only droppable types); a glossary placed on a page
  is never read by the scans.
- **A language with no glossary is "Not checked"**, never "Compliant": the reports say that nothing
  was checked and how to fix it.
