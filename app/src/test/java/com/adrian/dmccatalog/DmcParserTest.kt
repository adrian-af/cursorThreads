package com.adrian.dmccatalog

import com.adrian.dmccatalog.data.parseDmcChartMarkdown
import com.adrian.dmccatalog.ui.bestContrastTextColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DmcParserTest {
    @Test
    fun parser_extractsKnownRows() {
        val md = """
            | |  [DMC B5200](/dmc-colors/b5200) | Snow White | #fafaff |     |
            | |  [DMC 310](/dmc-colors/310) | Black | #0c0c0c |     |
        """.trimIndent()
        val rows = parseDmcChartMarkdown(md)
        assertEquals(2, rows.size)
        assertEquals("B5200", rows.first().code)
        assertEquals("#0c0c0c", rows.last().hex)
    }

    @Test
    fun contrast_selectsReadableColor() {
        val darkHex = "#0c0c0c"
        val lightHex = "#fafaff"
        val darkResult = bestContrastTextColor(darkHex)
        val lightResult = bestContrastTextColor(lightHex)
        assertTrue(darkResult != lightResult)
    }
}
