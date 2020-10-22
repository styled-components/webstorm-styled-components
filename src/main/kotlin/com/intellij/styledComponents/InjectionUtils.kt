package com.intellij.styledComponents

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import kotlin.math.max

const val EXTERNAL_FRAGMENT = "EXTERNAL_FRAGMENT"

fun getInjectionPlaces(quotedLiteral: PsiElement): List<StringPlace> {
  if (quotedLiteral is JSStringTemplateExpression) {
    val ranges = quotedLiteral.stringRangesWithEmpty
    val arguments = quotedLiteral.arguments

    // `${css`margin: none;`}` and ``
    if (ranges.size <= 2 && ranges.all { it.isEmpty }) {
      return emptyList()
    }

    return ranges.mapIndexed { i, textRange ->
      StringPlace(null, textRange, getArgumentPlaceholder(arguments.elementAtOrNull(i), i))
    }
  }

  val endOffset = max(quotedLiteral.textLength - 1, 1)
  return listOf(StringPlace(null, TextRange.create(1, endOffset), null))
}

private fun getArgumentPlaceholder(argument: JSExpression?, index: Int): String? {
  if (argument == null) return null

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