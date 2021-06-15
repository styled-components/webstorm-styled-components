package com.intellij.styledComponents

import com.intellij.lang.javascript.patterns.JSPatterns.*
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSLiteralExpressionKind
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.psi.*
import com.intellij.psi.css.resolve.CssClassOrIdReference
import com.intellij.psi.css.util.CssResolveUtil
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.util.ProcessingContext

internal class StyledComponentsReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(
      jsLiteralExpression().inside(
        jsProperty()
          .withName("className")
          .withParent(JSObjectLiteralExpression::class.java)
          .inside(jsArgument(jsReferenceExpression().withReferenceName("attrs"), 0))
      ).and(FilterPattern(object : ElementFilter {
        override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
          return element is JSLiteralExpression && element.isQuotedLiteral
        }

        override fun isClassAcceptable(hintClass: Class<*>?) = true
      })),
      StyledComponentsClassNamesReferenceProvider()
    )
  }
}

private class StyledComponentsClassNamesReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (element !is JSLiteralExpression) {
      return emptyArray()
    }

    val references = arrayListOf<PsiReference>()
    if (element.getExpressionKind(false) == JSLiteralExpressionKind.TEMPLATE_WITH_ARGS) {
      val templateExpression = element as JSStringTemplateExpression
      val text = templateExpression.text
      templateExpression.stringRanges.forEach {
        extractReferences(it.substring(text), references, element, it.startOffset)
      }
    }
    else {
      element.stringValue?.let {
        extractReferences(it, references, element, 1)
      }
    }

    return references.toTypedArray()
  }

  fun extractReferences(text: String, references: MutableList<PsiReference>, element: PsiElement, offset: Int) {
    CssResolveUtil.consumeClassNames(text, element) { _, range ->
      references.add(object : CssClassOrIdReference(element, range.shiftRight(offset)) {
        override fun isId(): Boolean {
          return false
        }
      })
    }
  }
}