package com.intellij.styledComponents

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6TaggedTemplateExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import com.intellij.util.containers.ContainerUtil
import java.util.*

internal fun taggedTemplate(name: String): ElementPattern<JSStringTemplateExpression> {
    return taggedTemplate(referenceExpression().withText(name))
}

internal fun taggedTemplate(tagPattern: ElementPattern<out JSExpression>): ElementPattern<JSStringTemplateExpression> {
    return PlatformPatterns.psiElement(JSStringTemplateExpression::class.java)
            .withParent(PlatformPatterns.psiElement(ES6TaggedTemplateExpression::class.java)
                    .withChild(tagPattern))
}

internal fun withReferenceName(name: String): ElementPattern<JSReferenceExpression> {
    return referenceExpression()
            .with(object : PatternCondition<JSReferenceExpression>("referenceName") {
                override fun accepts(referenceExpression: JSReferenceExpression, context: ProcessingContext): Boolean {
                    return StringUtil.equals(referenceExpression.referenceName, name)
                }
            })
}

internal fun referenceExpression() = PlatformPatterns.psiElement(JSReferenceExpression::class.java)!!
internal fun callExpression() = PlatformPatterns.psiElement(JSCallExpression::class.java)!!

internal fun withNameStartingWith(names: List<String>): ElementPattern<JSReferenceExpression> {
    return referenceExpression().with(object : PatternCondition<JSReferenceExpression>("nameStartingWith") {
        override fun accepts(referenceExpression: JSReferenceExpression, context: ProcessingContext): Boolean {
            return ContainerUtil.startsWith(getReferenceParts(referenceExpression), names)
        }
    })
}

internal fun getReferenceParts(jsReferenceExpression: JSReferenceExpression): List<String> {
    val nameParts = SmartList<String>()

    var ref: JSReferenceExpression? = jsReferenceExpression
    while (ref != null) {
        val name = ref.referenceName
        if (name.isNullOrBlank()) return ContainerUtil.emptyList()
        nameParts.add(name)
        val qualifier = ref.qualifier
        ref = qualifier as? JSReferenceExpression
                ?: PsiTreeUtil.findChildOfType(qualifier, JSReferenceExpression::class.java)
    }
    Collections.reverse(nameParts)
    return nameParts
}

fun jsxAttribute(name: String): ElementPattern<out PsiElement> {
    val cssAttributePattern = xmlAttributeValue(xmlAttribute(name)
            .withParent(xmlTag().with(object : PatternCondition<XmlTag>("isJsx") {
                override fun accepts(tag: XmlTag, context: ProcessingContext): Boolean {
                    return DialectDetector.isJSX(tag)
                }
            })))

    //matches 'plain' attribute: '<div css="value"/>'
    val stringValuedCssAttribute = cssAttributePattern
            .withChild(PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN))

    //matches JS literal inside JSX expression: '<div css={"value"}/>'
    val jsInCssAttributePattern = JSPatterns.jsLiteralExpression()
            .with(object : PatternCondition<JSLiteralExpression?>("isStringLiteral") {
                override fun accepts(literal: JSLiteralExpression, context: ProcessingContext?): Boolean {
                    return literal.isStringLiteral || literal is JSStringTemplateExpression
                }
            }).withAncestor(2, PlatformPatterns.psiElement(JSEmbeddedContent::class.java)
                    .withParent(cssAttributePattern))


    return PlatformPatterns.or(stringValuedCssAttribute, jsInCssAttributePattern)
}

fun jsxBodyText(tagName: String, vararg attributeNames: String): ElementPattern<out PsiElement> {
    return JSPatterns.jsLiteralExpression()
            .with(object : PatternCondition<JSLiteralExpression?>("isStringLiteral") {
                override fun accepts(literal: JSLiteralExpression, context: ProcessingContext?): Boolean {
                    return literal.isStringLiteral || literal is JSStringTemplateExpression
                }
            })
            .withAncestor(3, xmlTag().withName(tagName).withAnyAttribute(*attributeNames))
}