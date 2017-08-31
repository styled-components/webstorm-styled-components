import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase

class CompletionTest : LightCodeInsightFixtureTestCase() {
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
}
