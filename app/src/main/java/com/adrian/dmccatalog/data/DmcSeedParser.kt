package com.adrian.dmccatalog.data

private val rowRegex = Regex("""\|\s*\|\s*\[DMC (.+?)\]\(.+?\)\s*\|\s*(.+?)\s*\|\s*(#[0-9a-fA-F]{6})\s*\|""")

fun parseDmcChartMarkdown(markdown: String): List<ThreadEntity> {
    return markdown
        .lineSequence()
        .mapNotNull { rowRegex.find(it)?.groupValues }
        .map { groups ->
            ThreadEntity(
                code = groups[1].trim().uppercase(),
                name = groups[2].trim(),
                hex = groups[3].trim().lowercase()
            )
        }
        .distinctBy { it.code }
        .toList()
}
