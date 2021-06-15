package com.intellij.styledComponents

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CompletionTest : BasePlatformTestCase() {
  override fun getBasePath(): String = "plugins/styled-components/testData/completion"

  fun testCompletionAfterInterpolationExpressionInParentheses() {
    myFixture.configureByText("dummy.es6",
                              "const HeroImage = styled(Box)`\n" +
                              "  position: relative;\n" +
                              "  &:after {\n" +
                              "    position: abs\${'out'}te;\n" +
                              "    background-image: url('\${p => p.bgSrc}');" +
                              "    border-radius: 6px;\n" +
                              "    color: <caret>\n" +
                              "  }\n" +
                              "`")
    val lookupElements = myFixture.completeBasic().map { it.lookupString }
    assertContainsElements(lookupElements, "red", "blue")
  }

  fun testCssPropInJsx() {
    myFixture.configureByText("test.jsx", "<div <caret>/>")
    val lookupElements = myFixture.completeBasic().map { it.lookupString }
    assertContainsElements(lookupElements, "css")
  }

  fun testCompleteCssPropWithQuotesForJSXAttributeSetting() {
    CodeStyle.doWithTemporarySettings(myFixture.project, CodeStyleSettingsManager.createTestSettings(null), Runnable {
      val jsCodeStyleSettings = CodeStyle.getSettings(myFixture.project).getCustomSettings(JSCodeStyleSettings::class.java)
      jsCodeStyleSettings.JSX_ATTRIBUTE_VALUE = JSCodeStyleSettings.JSXAttributeValuePresentation.TYPE_BASED
      myFixture.configureByText("test.jsx", "<div <caret>/>")
      val cssItem = myFixture.completeBasic().find { it.lookupString == "css" }
      assertNotNull("expected 'css' item", cssItem)
      myFixture.lookup.currentItem = cssItem
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("<div css=\"\"/>")
    })
  }

  fun testNoCssPropInHtml() {
    myFixture.configureByText("test.html", "<div <caret>/>")
    val lookupElements = myFixture.completeBasic().map { it.lookupString }
    UsefulTestCase.assertDoesntContain(lookupElements, "css")
  }

  fun testAttrClassNames() {
    myFixture.copyFileToProject("styledComponentsStylesheet.css")
    doTest(arrayListOf("layout", "layout-wide", "layout-narrow"))
  }

  fun testAttrClassNamesNestedExpression() {
    myFixture.copyFileToProject("styledComponentsStylesheet.css")
    doTest(arrayListOf("layout-wide", "layout-narrow"))
  }

  fun testAttrClassNamesFunc() {
    myFixture.copyFileToProject("styledComponentsStylesheet.css")
    doTest(arrayListOf("col-xs", "col-sm"))
  }

  private fun doTest(expected: Collection<String>, ext: String = ".js") {
    val testName = "${getTestName(true)}$ext"
    myFixture.configureByFile(testName)
    val lookupElements = myFixture.completeBasic()?.map { it.lookupString } ?: emptyList()
    assertContainsElements(lookupElements, expected)
  }
}
