package com.intellij.StyledComponents

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSBinaryExpression
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6TaggedTemplateExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import com.intellij.util.containers.ContainerUtil
import java.util.*

fun taggedTemplate(name: String): ElementPattern<JSStringTemplateExpression> {
    return taggedTemplate(referenceExpression().withText(name))
}

fun taggedTemplate(tagPattern: ElementPattern<out JSExpression>): ElementPattern<JSStringTemplateExpression> {
    return PlatformPatterns.psiElement(JSStringTemplateExpression::class.java)
            .withParent(PlatformPatterns.psiElement(ES6TaggedTemplateExpression::class.java)
                    .withChild(tagPattern))
}
fun genericTaggedTemplate(tagPattern: ElementPattern<out JSExpression>): ElementPattern<JSStringTemplateExpression> {
    return PlatformPatterns.psiElement(JSStringTemplateExpression::class.java)
            .withParent(PlatformPatterns.psiElement(JSBinaryExpression::class.java)
                    .withChild(PlatformPatterns.psiElement(JSBinaryExpression::class.java)
                            .withChild(PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                    .withChild(tagPattern))
                            .withChild(PlatformPatterns.psiElement(JSTokenTypes.LT)))
                    .withChild(PlatformPatterns.psiElement(JSTokenTypes.GT)))
}
fun withReferenceName(name: String): ElementPattern<JSReferenceExpression> {
    return referenceExpression()
            .with(object : PatternCondition<JSReferenceExpression>("referenceName") {
                override fun accepts(referenceExpression: JSReferenceExpression, context: ProcessingContext): Boolean {
                    return StringUtil.equals(referenceExpression.referenceName, name)
                }
            })
}

fun referenceExpression() = PlatformPatterns.psiElement(JSReferenceExpression::class.java)!!
fun callExpression() = PlatformPatterns.psiElement(JSCallExpression::class.java)!!

fun withNameStartingWith(vararg components: String): ElementPattern<JSReferenceExpression> {
    val componentsList = ContainerUtil.list(*components)
    return referenceExpression().with(object : PatternCondition<JSReferenceExpression>("nameStartingWith") {
        override fun accepts(referenceExpression: JSReferenceExpression, context: ProcessingContext): Boolean {
            return ContainerUtil.startsWith(getReferenceParts(referenceExpression), componentsList)
        }
    })
}

fun getReferenceParts(jsReferenceExpression: JSReferenceExpression): List<String> {
    val nameParts = SmartList<String>()

    var ref: JSReferenceExpression? = jsReferenceExpression
    while (ref != null) {
        val name = ref.referenceName
        if (StringUtil.isEmptyOrSpaces(name)) return ContainerUtil.emptyList()
        nameParts.add(name)
        val qualifier = ref.qualifier
        ref = qualifier as? JSReferenceExpression
                ?: PsiTreeUtil.findChildOfType(qualifier, JSReferenceExpression::class.java)
    }
    Collections.reverse(nameParts)
    return nameParts
}