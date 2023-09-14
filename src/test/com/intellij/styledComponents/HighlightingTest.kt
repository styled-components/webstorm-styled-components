package com.intellij.styledComponents

import com.intellij.psi.css.inspections.CssUnknownPropertyInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidPropertyValueInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HighlightingTest : BasePlatformTestCase() {

  fun testWithoutArguments_ErrorsHighlighted() {
    myFixture.enableInspections(CssInvalidPropertyValueInspection::class.java, CssUnknownPropertyInspection::class.java)
    doTest("""
      var someCss = css`div {
        color: <error>not-a-color</error>;
        .nested {
          <warning>unknown</warning>: 0;
        }
        @container sidebar (width < calc(64px + 12ch)) {
          display: none;
        }
      }`
  """.trimIndent())
  }

    fun testErrorSurroundsInterpolationArgument_NotHighlighted() {
        myFixture.enableInspections(CssInvalidPropertyValueInspection::class.java)
        doTest("var someCss = css`\n" +
                "//should not highlight\n" +
                "withArgument{\n" +
                "  border: 5px \${foobar} red;\n" +
                "},\n" +
                "//should highlight\n" +
                "withoutArgument {\n" +
                "  border: 5px<error> foobar-not-acceptable red</error>;\n" +
                "};;`")
    }

    fun testErrorAdjacentToInterpolationArgument_NotHighlighted() {
        doTest("var styledSomething = styled.something`\n" +
                "  perspective: 1000px;\n" +
                "  \${value}\n" +
                "  \${anotherValue}\n" +
                "`\n" +
                "const Triangle = styled.span`\n" +
                "  \${({ right }) => (right ? 'right: 0;' : 'left: 0;')}\n" +
                "`")
    }

    private fun doTest(expected: String) {
        myFixture.setCaresAboutInjection(false)
        myFixture.configureByText("dummy.es6", expected)
        myFixture.testHighlighting(true, false, true)
    }

}