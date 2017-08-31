package com.intellij.StyledComponents

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.util.ArrayUtil
import com.intellij.util.containers.ContainerUtil
import java.util.ArrayList

val INJECTED_FILE_RANGES_KEY = Key<List<TextRange>?>("INJECTED_FILE_RANGES_KEY")
private val EXTERNAL_FRAGMENT = "EXTERNAL_FRAGMENT"

fun getInjectionPlaces(myQuotedLiteral: JSLiteralExpression): List<StringPlace> {
    if (myQuotedLiteral is JSStringTemplateExpression) {
        val templateExpression = myQuotedLiteral
        val ranges = templateExpression.stringRanges
        if (ranges.isNotEmpty()) {
            val result = ArrayList<StringPlace>(ranges.size)
            val quotedLiteralNode = myQuotedLiteral.getNode()
            val backquote = quotedLiteralNode.findChildByType(JSTokenTypes.BACKQUOTE)
            val backquoteOffset = if (backquote != null) backquote.startOffset - quotedLiteralNode.startOffset else -1
            for (i in ranges.indices) {
                val range = ranges[i]
                val prefix = if (i == 0 && range.startOffset > backquoteOffset + 1) EXTERNAL_FRAGMENT else null
                val suffix = if (i < ranges.size - 1 || range.endOffset < myQuotedLiteral.getTextLength() - 1) EXTERNAL_FRAGMENT else null
                result.add(StringPlace(prefix, range, suffix))
            }
            return result
        }
        if (!ArrayUtil.isEmpty(templateExpression.arguments)) {
            return ContainerUtil.emptyList()
        }
    }
    val endOffset = Math.max(myQuotedLiteral.textLength - 1, 1)
    return listOf(StringPlace(null, TextRange.create(1, endOffset), null))
}


data class StringPlace(val prefix: String?, val range: TextRange, val suffix: String?) 