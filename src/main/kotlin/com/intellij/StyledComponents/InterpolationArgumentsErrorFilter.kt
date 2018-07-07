package com.intellij.StyledComponents

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter
import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile

class InterpolationArgumentsErrorFilter : HighlightErrorFilter(), HighlightInfoFilter {
    override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
        val acceptedRanges = element.containingFile.getUserData(INJECTED_FILE_RANGES_KEY)
        if (acceptedRanges == null) {
            return true
        }
        return acceptedRanges.any { range -> range.contains(element.textRange) }
    }

    override fun accept(highlightInfo: HighlightInfo, file: PsiFile?): Boolean {
        val acceptedRanges = file?.getUserData(INJECTED_FILE_RANGES_KEY)
        if (acceptedRanges == null) {
            return true
        }
        if (highlightInfo.severity === HighlightSeverity.WARNING
                || highlightInfo.severity === HighlightSeverity.WEAK_WARNING
                || highlightInfo.severity === HighlightSeverity.ERROR) {
            return acceptedRanges.any { highlightInfo.startOffset > it.startOffset
                    && highlightInfo.endOffset < it.endOffset }
        }
        return true
    }

}

