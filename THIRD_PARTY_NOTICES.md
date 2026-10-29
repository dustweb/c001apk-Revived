# Third-party notices

The SZLM registration wire format and the sample report in
`app/src/main/assets/szlm_report.json` are based on
[kongwufang/szlm_res v1.0.0](https://github.com/kongwufang/szlm_res/releases/tag/v1.0.0)
(`getduid.py`, MIT, Copyright 2026 kongwufang).
The complete license is distributed in `app/src/main/assets/licenses/szlm_res-MIT.txt`.

The Kotlin client and application integration are implemented in this project.
Registration runs only when the user requests it. It does not collect account
credentials or real device identifiers and does not rotate identifiers in response
to verification challenges. Existing identifiers are preserved on failure.
