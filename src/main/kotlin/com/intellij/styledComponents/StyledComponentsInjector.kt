package com.intellij.styledComponents

import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.injections.JSFormattableInjectionUtil
import com.intellij.lang.javascript.injections.StringInterpolationErrorFilter
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.XmlAttributeValue

const val COMPONENT_PROPS_PREFIX = "div {"
const val COMPONENT_PROPS_SUFFIX = "}"

class StyledComponentsInjector : MultiHostInjector {
    companion object {
        private val styledPattern = withNameStartingWith(listOf("styled"))
        private val builtinPlaces: List<PlaceInfo> = listOf(
                PlaceInfo(taggedTemplate(PlatformPatterns.or(styledPattern,
                        PlatformPatterns.psiElement(JSExpression::class.java)
                                .withFirstChild(styledPattern))), COMPONENT_PROPS_PREFIX, COMPONENT_PROPS_SUFFIX),
                PlaceInfo(jsxAttribute("css"), COMPONENT_PROPS_PREFIX, COMPONENT_PROPS_SUFFIX),
                PlaceInfo(taggedTemplate(withReferenceName("extend")), COMPONENT_PROPS_PREFIX, COMPONENT_PROPS_SUFFIX),
                PlaceInfo(taggedTemplate(callExpression().withChild(withReferenceName("attrs"))), COMPONENT_PROPS_PREFIX, COMPONENT_PROPS_SUFFIX),
                PlaceInfo(taggedTemplate("css"), COMPONENT_PROPS_PREFIX, COMPONENT_PROPS_SUFFIX),
                PlaceInfo(taggedTemplate("injectGlobal")),
                PlaceInfo(taggedTemplate("createGlobalStyle")),
                PlaceInfo(taggedTemplate("keyframes"), "@keyframes foo {", "}"),
                PlaceInfo(jsxBodyText("style", "jsx"))
        )

        fun matchInjectionTarget(injectionHost: PsiLanguageInjectionHost): PlaceInfo? {
            val customInjections = CustomInjectionsConfiguration.instance(injectionHost.project)
            return builtinPlaces.find { (elementPattern) -> elementPattern.accepts(injectionHost) }
                    ?: customInjections.getInjectionPlaces().find { (elementPattern) -> elementPattern.accepts(injectionHost) }
        }
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(JSLiteralExpression::class.java, XmlAttributeValue::class.java)
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, injectionHost: PsiElement) {
        if (injectionHost !is PsiLanguageInjectionHost) return
        
        val injectionLanguage = Language.findLanguageByID("LESS") ?: CSSLanguage.INSTANCE
        val acceptedPattern = matchInjectionTarget(injectionHost) ?: return
        val stringPlaces = getInjectionPlaces(injectionHost)
        if (stringPlaces.isEmpty())
            return

        registrar.startInjecting(injectionLanguage)
        stringPlaces.forEachIndexed { index, (prefix, range, suffix) ->
            val thePrefix = if (index == 0) acceptedPattern.prefix + prefix.orEmpty() else prefix
            val theSuffix = if (index == stringPlaces.size - 1) suffix.orEmpty() + acceptedPattern.suffix else suffix
            registrar.addPlace(thePrefix, theSuffix, injectionHost, range)
        }
        registrar.doneInjecting()

        if (stringPlaces.size > 1) {
            StringInterpolationErrorFilter.register(injectionHost, injectionLanguage)
            StyledComponentsErrorFilter.register(injectionHost, injectionLanguage)
        }

        JSFormattableInjectionUtil.setReformattableInjection(injectionHost, injectionLanguage)
    }

}
