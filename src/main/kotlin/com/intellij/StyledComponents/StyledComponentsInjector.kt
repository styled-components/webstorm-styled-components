package com.intellij.StyledComponents

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.less.LESSLanguage

class StyledComponentsInjector : MultiHostInjector {
    companion object {
        private val styledPattern = withNameStartingWith("styled")
        val places: List<PlaceInfo> = listOf(
                PlaceInfo(taggedTemplate(PlatformPatterns.or(styledPattern,
                        PlatformPatterns.psiElement(JSExpression::class.java)
                                .withFirstChild(styledPattern))), "div {", "}"),
                PlaceInfo(taggedTemplate(withReferenceName("extend")), "div {", "}"),
                PlaceInfo(taggedTemplate("css")),
                PlaceInfo(taggedTemplate("injectGlobal")),
                PlaceInfo(taggedTemplate("keyframes"), "@keyframes foo {", "}")
        )
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(JSStringTemplateExpression::class.java)
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, injectionHost: PsiElement) {
        if (injectionHost !is JSStringTemplateExpression)
            return
        val acceptedPattern = places.find { (elementPattern) -> elementPattern.accepts(injectionHost) }
        if (acceptedPattern != null) {
            val stringPlaces = getInjectionPlaces(injectionHost)
            registrar.startInjecting(LESSLanguage.INSTANCE)
            stringPlaces.forEachIndexed { index, (prefix, range, suffix) ->
                val thePrefix = if (index == 0) acceptedPattern.prefix else prefix
                val theSuffix = if (index == stringPlaces.size - 1) acceptedPattern.suffix else suffix
                registrar.addPlace(thePrefix, theSuffix, injectionHost, range)
            }
            registrar.doneInjecting()
        }

    }
    data class PlaceInfo(val elementPattern: ElementPattern<JSStringTemplateExpression>,
                         val prefix: String? = null,
                         val suffix: String? = null)

}