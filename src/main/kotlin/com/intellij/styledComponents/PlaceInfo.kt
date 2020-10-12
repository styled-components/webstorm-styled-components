package com.intellij.styledComponents

import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement

data class PlaceInfo(val elementPattern: ElementPattern<out PsiElement>,
                     val prefix: String = "",
                     val suffix: String = "")