package com.intellij.styledComponents

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.ArrayUtil
import com.intellij.util.containers.ContainerUtil
import java.util.*
import kotlin.math.max

private const val EXTERNAL_FRAGMENT = "EXTERNAL_FRAGMENT"

fun getInjectionPlaces(quotedLiteral: PsiElement): List<StringPlace> {
    if (quotedLiteral is JSStringTemplateExpression) {
        val ranges = quotedLiteral.stringRanges
        val arguments = quotedLiteral.arguments

        if (ranges.isNotEmpty()) {
            val result = ArrayList<StringPlace>(ranges.size)
            var lastArgumentIndex = -1

            for (i in ranges.indices) {
                val range = ranges[i]

                // styled.div`padding: ${'none'}${'IGNORED_ARGUMENT'}${'display'}: none;`
                //           |_________|_______|____________________|___________|______|
                //              Text    Suffix         Ignored          Prefix    Text
                //          |__________________|                    |__________________|
                //                1 fragment                               2 fragment

                var currentIndex = adjustPrecedingArgumentIndex(lastArgumentIndex, range, arguments)
                val prefix = if (currentIndex != lastArgumentIndex)
                    getArgumentPlaceholder(arguments.elementAtOrNull(currentIndex), currentIndex) else null

                val suffix = getArgumentPlaceholder(arguments.elementAtOrNull(++currentIndex), currentIndex)

                lastArgumentIndex = currentIndex
                result.add(StringPlace(prefix, range, suffix))
            }
            return result
        }
        if (!ArrayUtil.isEmpty(arguments)) {
            return ContainerUtil.emptyList()
        }
    }

    val endOffset = max(quotedLiteral.textLength - 1, 1)
    return listOf(StringPlace(null, TextRange.create(1, endOffset), null))
}


private fun adjustPrecedingArgumentIndex(startIndex: Int, range: TextRange, arguments: Array<JSExpression>): Int {
    var current = startIndex

    while (true) {
        val argument = arguments.getOrNull(current + 1)
        if (argument == null || argument.textRangeInParent.startOffset > range.endOffset) return current
        current++
    }
}

private fun getArgumentPlaceholder(argument: JSExpression?, index: Int): String? {
    if (argument == null) return null

    var hasChildInjection = false
    argument.accept(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(element: PsiElement) {
            if (element is PsiLanguageInjectionHost && StyledComponentsInjector.matchInjectionTarget(element) != null) {
                hasChildInjection = true
                stopWalking()
            }

            super.visitElement(element)
        }
    })
    if (hasChildInjection) return null

    if (argument is JSLiteralExpression) {
        val value = argument.value
        if (value != null) {
            return value.toString()
        }
    }

    if (argument is JSReferenceExpression && argument.qualifier == null) {
        val referenceName = argument.referenceName
        if (referenceName != null) {
            return referenceName
        }
    }

    return "${EXTERNAL_FRAGMENT}_$index"
}

data class StringPlace(val prefix: String?, val range: TextRange, val suffix: String?)