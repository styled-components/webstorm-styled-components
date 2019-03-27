package com.intellij.styledComponents

import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.patterns.ElementPattern

data class PlaceInfo(val elementPattern: ElementPattern<JSStringTemplateExpression>,
                     val prefix: String = "",
                     val suffix: String = "")