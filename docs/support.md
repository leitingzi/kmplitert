# Support and Contributing

## Report an issue

Open a [GitHub issue](https://github.com/leitingzi/kmplitert/issues) and include, where possible:

- KMPLiteRT version, target platform, and device architecture.
- Kotlin, Gradle, and LiteRT runtime versions.
- Minimal reproduction steps, model input/output details, and the complete error stack.
- Whether the issue only occurs with a particular delegate (CPU/GPU/NPU).

Before sharing models or device information, make sure they contain no private or sensitive data.

## Contribute

1. Fork the repository and create a feature branch.
2. Change code or documentation and run relevant tests.
3. Submit a clear change description and open a pull request.

When modifying platform implementations, keep shared API behavior consistent where possible and validate on available targets.

## Maintain the documentation

Install the documentation dependencies from [requirements-docs.txt](https://github.com/leitingzi/kmplitert/blob/master/requirements-docs.txt), then run from the repository root:

```shell
python -m mkdocs serve
python -m mkdocs build --strict
python .github/scripts/check_docs_i18n.py
```

English pages use `*.md`; their Chinese counterparts use `*.zh.md`. Keep both versions, their links, and shared anchors synchronized. The documentation workflow runs a strict build and deploys to GitHub Pages after relevant changes on `master`. When updating Core versions, targets, runtime, API, or tests, also review installation, the capability matrix, and limitations; update the JVM documentation example test when changing quick-start inference code. Do not mark unverified devices or accelerators as passed.

## License

KMPLiteRT is licensed under [Apache License 2.0](https://github.com/leitingzi/kmplitert/blob/master/LICENSE).
