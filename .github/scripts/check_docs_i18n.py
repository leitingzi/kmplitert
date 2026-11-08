from html.parser import HTMLParser
import json
from pathlib import Path
import sys
from urllib.parse import urljoin

import yaml


ROOT = Path(__file__).resolve().parents[2]
DOCS = ROOT / "docs"


class PageParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.language = None
        self.alternates = {}

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == "html":
            self.language = attrs.get("lang")
        if tag == "a" and attrs.get("hreflang"):
            self.alternates[attrs["hreflang"]] = attrs.get("href")


def main():
    site = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else ROOT / "site"
    config = yaml.safe_load((ROOT / "mkdocs.yml").read_text(encoding="utf-8"))
    site_url = config["site_url"].rstrip("/") + "/"
    english = {
        path.relative_to(DOCS)
        for path in DOCS.rglob("*.md")
        if not path.name.endswith(".zh.md")
    }
    chinese = {
        path.relative_to(DOCS).with_name(path.name.removesuffix(".zh.md") + ".md")
        for path in DOCS.rglob("*.zh.md")
    }
    if not english or english != chinese:
        raise ValueError(f"Unpaired translations: English-only={english - chinese}, Chinese-only={chinese - english}")

    for source in sorted(english):
        page = source.with_suffix("")
        if page.name == "index":
            page = page.parent
        page_path = "" if str(page) == "." else page.as_posix() + "/"
        for locale, prefix in (("en", ""), ("zh", "zh/")):
            relative = prefix + page_path
            html = site / relative / "index.html"
            parser = PageParser()
            parser.feed(html.read_text(encoding="utf-8"))
            if parser.language != locale:
                raise ValueError(f"{html}: expected language {locale}, got {parser.language}")
            current_url = urljoin(site_url, relative)
            for target_locale, target_prefix in (("en", ""), ("zh", "zh/")):
                link = parser.alternates.get(target_locale)
                expected = urljoin(site_url, target_prefix + page_path)
                if not link or urljoin(current_url, link) != expected:
                    raise ValueError(f"{html}: invalid {target_locale} alternate {link!r}, expected {expected}")

    search = json.loads((site / "search" / "search_index.json").read_text(encoding="utf-8"))
    locations = [entry["location"] for entry in search["docs"]]
    if not any(path.startswith("zh/") for path in locations) or not any(
        not path.startswith("zh/") for path in locations
    ):
        raise ValueError("Search index must include both English and Chinese pages")
    print(f"Verified {len(english)} bilingual pages, contextual language links, and search index.")


if __name__ == "__main__":
    main()
