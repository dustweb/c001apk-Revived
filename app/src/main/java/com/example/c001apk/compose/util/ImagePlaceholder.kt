package com.example.c001apk.compose.util

private val imagePlaceholderLink = Regex(
    """<a\b[^>]*>\s*\[图片]\s*</a>""",
    RegexOption.IGNORE_CASE,
)
private val imagePlaceholderLine = Regex(
    """(^|\n|<br\s*/?>)\s*\[图片]\s*(?=$|\n|<br\s*/?>)""",
    RegexOption.IGNORE_CASE,
)
private val edgeBreaks = Regex("""^(?:\s|<br\s*/?>|&nbsp;)+|(?:\s|<br\s*/?>|&nbsp;)+$""", RegexOption.IGNORE_CASE)

/** Keep image links in text-only previews; omit the placeholder beside rendered attachments. */
fun String.withoutImagePlaceholder(imagesDisplayed: Boolean): String {
    if (!imagesDisplayed) return this
    return replace(imagePlaceholderLink, "")
        .replace(imagePlaceholderLine) { it.groupValues[1] }
        .replace(edgeBreaks, "")
}
