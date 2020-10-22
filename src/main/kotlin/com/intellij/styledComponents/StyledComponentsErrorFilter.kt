package com.intellij.styledComponents

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.lang.Language
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.css.CssDeclaration
import com.intellij.psi.css.CssTerm
import com.intellij.psi.css.impl.CssElementTypes
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore

class StyledComponentsErrorFilter : HighlightErrorFilter() {
  companion object {
    private val STYLED_COMPONENTS_INJECTION = Key.create<Boolean>("styled.components.injection")

    fun register(host: PsiLanguageInjectionHost, language: Language) {
      InjectedLanguageUtil.getCachedInjectedFileWithLanguage(host, language)?.putUserData(STYLED_COMPONENTS_INJECTION, true)
    }
  }

  override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
    if (!STYLED_COMPONENTS_INJECTION[element.containingFile]) return true
    if (PsiUtilCore.getElementType(PsiTreeUtil.skipWhitespacesAndCommentsForward(element)) != CssElementTypes.CSS_COLON) return true

    val prevElement = PsiTreeUtil.skipWhitespacesAndCommentsBackward(element)?.takeIf { it is CssTerm } ?: return true
    val declaration = prevElement.parent?.parent as? CssDeclaration ?: return true

    return !declaration.propertyName.startsWith(EXTERNAL_FRAGMENT)
  }
}