import com.intellij.psi.css.inspections.invalid.CssInvalidPropertyValueInspection
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class HighlightingTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testWithoutArguments_ErrorsHighlighted() {
        myFixture.enableInspections(CssInvalidPropertyValueInspection::class.java)
        doTest("var someCss = css`div {\n" +
                "  color:<error>not-a-color</error>\n" +
                "}`")
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