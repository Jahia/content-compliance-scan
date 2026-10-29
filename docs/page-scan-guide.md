# The page scan, for editors

**Compliance scan** in the Page Builder header checks the page you are editing, in the language you
are editing, against the site's glossaries of non-authorised terms. It is informational: it tells
you what to fix before publishing, it does not stop you publishing.

The button shows on pages you can edit, on sites where the module is enabled.

## The three results

| Result | Meaning |
|---|---|
| **Compliant** | Glossaries exist for this language and no text of the page uses their terms |
| **Not compliant** | At least one term is used. The banner says not to publish the page yet |
| **Not checked** | No glossary exists for this language: nothing was checked. Ask whoever maintains the terms |

The summary line counts the uses and the contents they are in; **Glossaries applied** lists the
glossaries used (click one to open it).

## Reading an issue

Issues are grouped by content. For each content:

- its name, type and path, and **Edit**, which opens it in Content Editor. When you close the editor,
  the scan runs again;
- each use: the field ("Text", "Title"), the sentence with the term highlighted, the term, the
  reason and the suggested wording, and the glossary when several apply. A term two glossaries
  share is listed once for each, with each glossary's reason.

A content can carry a second label:

| Label | What it means for you |
|---|---|
| *(none)* | Stored on this page: fixing it here fixes it for this page, published with the page |
| **Reused content** | Shown here through a content reference. Editing it changes every page that reuses it, and it is published on its own |
| **Shared content** | Stored on another page (the header and footer on the home page) or shown by a listing. Editing it changes every page that shows it; it is published with the page that stores it |

## What the page scan reads

Everything a visitor can read on the page in this language: the page title and SEO description, every
text, text area and rich text of the content on the page, the content it reuses, the header and
footer, and the items that listings show. Choice lists, links and other technical values are not
read. Images are not read (their alternative text included).

**Scan again** reruns the scan, after you edit content without the Edit button for instance.
Opened from jContent's page list instead of Page Builder, the scan cannot see the items listings
show; the dialog says so.
