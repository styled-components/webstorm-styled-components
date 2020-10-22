package com.intellij.styledComponents

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.containers.ContainerUtil
import org.junit.Assert

class InjectionTest : BasePlatformTestCase() {

    fun testTemplateArgumentIsWholeRange() {
        doTest("let css = css`\${someVariable}`")
        doTest("let globalCss = injectGlobal`\${someVariable}`")
    }

    fun testCss() {
        doTest("let css = css`\n" +
                "  color:red;\n" +
                "  width:100px;\n" +
                "  height:100px;`", "div {\n" +
                "  color:red;\n" +
                "  width:100px;\n" +
                "  height:100px;}")
    }

    fun testSimpleComponent() {
        doTest("const Title = styled.h1`\n" +
                "  font-size: 1.5em;\n" +
                "`;",
                "div {\n" +
                        "  font-size: 1.5em;\n" +
                        "}")
    }

    fun testComponentWithArgs() {
        doTest("const Button = styled.button`\n" +
                "    /* Adapt the colours based on primary prop */\n" +
                "   background: \${props => props.primary ? 'palevioletred' : 'white'};\n" +
                "   color: \${props => props.primary ? 'white' : 'palevioletred'};\n" +
                "    " +
                "    font-size: 1em;\n" +
                "`;", "div {\n" +
                "    /* Adapt the colours based on primary prop */\n" +
                "   background: EXTERNAL_FRAGMENT_0;\n" +
                "   color: EXTERNAL_FRAGMENT_1;\n" +
                "        font-size: 1em;\n" +
                "}")
    }

    fun testComplexExpression() {
        doTest("const Input = styled.input.attrs({\n" +
                "    type: 'password',\n" +
                "\n" +
                "    // or we can define dynamic ones\n" +
                "    margin: props => props.size || '1em',\n" +
                "    padding: props => props.size || '1em',\n" +
                "})`\n" +
                "    color: palevioletred;\n" +
                "`;", "div {\n" +
                "    color: palevioletred;\n" +
                "}")
    }

    fun testComplexExpression2() {
        doTest("const ContactMenuIcon = ((styled(Icon)))" +
                ".attrs({ iconName: 'contact_card' })`\n" +
                "  line-height: 0;\n" +
                "`", "div {\n" +
                "  line-height: 0;\n" +
                "}")
    }

    fun testKeyframes() {
        doTest("const rotate360 = keyframes`\n" +
                "    from{\n" +
                "      transform:rotate(0deg);\n" +
                "    }\n" +
                "\n" +
                "    to{\n" +
                "      transform:rotate(360deg);\n" +
                "    }\n" +
                "`;",
                "@keyframes foo {\n" +
                        "    from{\n" +
                        "      transform:rotate(0deg);\n" +
                        "    }\n" +
                        "\n" +
                        "    to{\n" +
                        "      transform:rotate(360deg);\n" +
                        "    }\n" +
                        "}")

    }

    fun testExtendsComponent() {
        doTest("const TomatoButton = Button.extend`\n" +
                "   color: tomato;\n" +
                "   border-color: tomato;\n" +
                "`;",
                "div {\n" +
                        "   color: tomato;\n" +
                        "   border-color: tomato;\n" +
                        "}")
    }

    fun testComponentAttrs() {
        doTest("const div = styled.div;\n" +
                "const FilterIcon = div.attrs({ iconName: 'filter' })`\n" +
                "  line-height: 0;\n" +
                "`", "div {\n" +
                "  line-height: 0;\n" +
                "}")
    }

    fun testInjectGlobal() {
        doTest("injectGlobal`\n" +
                "  div{\n" +
                "    color:red\n" +
                "  }\n" +
                "`", "\n" +
                "  div{\n" +
                "    color:red\n" +
                "  }\n")
    }

    fun testTemplateArgsAtStartEndOfString() {
        doTest("let atStart = styled.div`\${getPropName()}:red`\n" +
                "let atEnd = styled.div`color:\${getColor()}`\n",
                "div {EXTERNAL_FRAGMENT_0:red}",
                "div {color:EXTERNAL_FRAGMENT_0}")
    }

    fun testWithCustomInjectionMediaQuery() {
        setCustomInjectionsConfiguration("media")
        doTest("const Container = styled.div`\n" +
                "  color: #333;\n" +
                "  \${media.desktop `padding: 0 20px;` }\n" +
                "`", "div {\n" +
                "  color: #333;\n" +
                "  EXTERNAL_FRAGMENT_0\n" +
                "}", "div {padding: 0 20px;}")
    }

    fun testWithUnqualifiedCustomTag() {
        setCustomInjectionsConfiguration("sc")
        doTest("const Container = sc`color: #333;`;", "div {color: #333;}")
    }

    fun testCustomInjectionWithComplexTag() {
        setCustomInjectionsConfiguration("bp")
        doTest("const Container = styled.div`\n" +
                "  color: #333;\n" +
                "  \${bp(media.tablet)`padding: 0 20px;` }\n" +
                "`", "div {\n" +
                "  color: #333;\n" +
                "  EXTERNAL_FRAGMENT_0\n" +
                "}", "div {padding: 0 20px;}")
    }

    fun testCssProperty_DoubleQuotedAttributeValue() {
        doTest("<div css=\"color:red\"/>", "div {color:red}")
    }

    fun testCssProperty_SingleQuotedAttributeValue() {
        doTest("<div css='color:red'/>", "div {color:red}")
    }

    fun testCssProperty_TemplateStringInValue() {
        doTest("<div css={`color:red`}/>", "div {color:red}")
    }

    fun testCssProperty_PlainJSStringInValue() {
        doTest("<div css={'color:red'}/>", "div {color:red}")
    }

    fun testNoCssPropertyInjectionInHtml() {
        doTestWithExtension("<div css='color:red'/>", "html", emptyArray())
    }

    fun testNoInjectionWithObjectInCssProperty() {
        doTest("<div css={{color:'red'}}/>")
    }

    fun testStyledJsx() {
        doTest("<style jsx>{`\n" +
                "  .container {\n" +
                "    margin: 0 auto;\n" +
                "    width: 880px\n" +
                "  }\n" +
                "`}</style>", "\n" +
                "  .container {\n" +
                "    margin: 0 auto;\n" +
                "    width: 880px\n" +
                "  }\n")
    }

    fun testArgumentNestedInjectionBeforeProperty() {
        doTest("const ErrorDiv = styled.div`\n" +
                "  \${props =>\n" +
                "    css`\n" +
                "      color: red;    \n" +
                "    `}\n" +
                "  color: blue; \n" +
                "`;", "div {\n" +
                "  EXTERNAL_FRAGMENT_0\n" +
                "  color: blue; \n" +
                "}", "div {\n" +
                "      color: red;    \n" +
                "    }"
        )
    }

    fun testArgumentNestedInjectionAfterProperty() {
        doTest("const ErrorDiv = styled.div`\n" +
                "  color: blue;\n" +
                "  \${props =>\n" +
                "    css`\n" +
                "      color: red;\n" +
                "    `}\n" +
                "`;", "div {\n" +
                "  color: blue;\n" +
                "  EXTERNAL_FRAGMENT_0\n" +
                "}", "div {\n" +
                "      color: red;\n" +
                "    }"
        )
    }

    fun testArgumentNestedInjectionOnlyArgument() {
        doTest("const OptionLabel = styled.div`\n" +
                "  \${(props) => css`\n" +
                "    margin-bottom: 0.3em;\n" +
                "  `}\n" +
                "`;", "div {\n" +
                "  EXTERNAL_FRAGMENT_0\n" +
                "}", "div {\n" +
                "    margin-bottom: 0.3em;\n" +
                "  }"
        )
    }

    fun testArgumentNestedInjectionLeadingArgument() {
        doTest("const OptionLabel = styled.div`\${(props) => css`margin-bottom: 0.3em;`} `;",
                "div {EXTERNAL_FRAGMENT_0 }",
                "div {margin-bottom: 0.3em;}"
        )
    }

    fun testArgumentNestedInjectionTrailingArgument() {
        doTest("const OptionLabel = styled.div` \${(props) => css`margin-bottom: 0.3em;`}`;",
                "div { EXTERNAL_FRAGMENT_0}",
                "div {margin-bottom: 0.3em;}"
        )
    }

    fun testArgumentNestedInjectionLeadingAndTrailingArgument() {
        doTest("const OptionLabel = styled.div`\${(props) => css`margin-bottom: 0.3em;`} padding: \${(props) => `5px;`}`;",
                "div {EXTERNAL_FRAGMENT_0 padding: EXTERNAL_FRAGMENT_1}",
                "div {margin-bottom: 0.3em;}"
        )
    }

    fun testArgumentNestedInjectionAdjacentArguments() {
        doTest("const OptionLabel = styled.div`padding: 3px; " +
                "\${(props) => css`margin-bottom: 0.3em;`}\${'BETWEEN-'}\${props => 'display'}: none;`;",
                "div {padding: 3px; EXTERNAL_FRAGMENT_0BETWEEN-EXTERNAL_FRAGMENT_2: none;}",
                "div {margin-bottom: 0.3em;}"
        )
    }

    fun testArgumentNestedInjectionAdjacentArgumentsLeading() {
        doTest("const OptionLabel = styled.div`" +
                "\${(props) => css`margin-bottom: 0.3em;`}\${'margin'}: 20px;\${props => 'display'}: none;`;",
                "div {EXTERNAL_FRAGMENT_0margin: 20px;EXTERNAL_FRAGMENT_2: none;}",
                "div {margin-bottom: 0.3em;}"
        )
    }

    fun testArgumentNestedInjectionAdjacentArgumentsTrailing() {
        doTest("const OptionLabel = styled.div`padding: 3px; " +
                "\${(props) => css`margin-bottom: 0.3em;`}\${'BETWEEN-'}\${props => 'display'}: none; \${'background: red'}`;",
                "div {padding: 3px; EXTERNAL_FRAGMENT_0BETWEEN-EXTERNAL_FRAGMENT_2: none; background: red}",
                "div {margin-bottom: 0.3em;}"
        )
    }

    fun testArgumentNestedInjectionAdjacentArgumentsWithInjectionInBetween() {
        doTest("const OptionLabel = styled.div`padding: 3px; " +
                "\${(props) => css`margin-bottom: 0.3em;`}\${(props) => css`margin-top: 0.3em;`}\${props => 'display'}: none;`;",
                "div {padding: 3px; EXTERNAL_FRAGMENT_0EXTERNAL_FRAGMENT_1EXTERNAL_FRAGMENT_2: none;}",
                "div {margin-bottom: 0.3em;}",
                "div {margin-top: 0.3em;}"
        )
    }

    fun testArgumentNestedPlainStringCss() {
        doTest("const ErrorDiv = styled.div`\n" +
                "  \${props =>\n" +
                "    `\n" +
                "      color: red;    \n" +
                "    `};\n" +
                "  color: blue; \n" +
                "`;", "div {\n" +
                "  EXTERNAL_FRAGMENT_0;\n" +
                "  color: blue; \n" +
                "}"
        )
    }

    fun testArgumentsInlinedToInjection() {
        doTest("styled.div`\${false}: absolute;\${reference}: none;`",
                "div {false: absolute;reference: none;}"
        )
    }

    private fun setCustomInjectionsConfiguration(vararg prefixes: String) {
        val configuration = CustomInjectionsConfiguration.instance(myFixture.project)
        val previousPrefixes = configuration.getTagPrefixes()
        configuration.setTagPrefixes(arrayOf(*prefixes))
        Disposer.register(myFixture.testRootDisposable, Disposable { configuration.setTagPrefixes(previousPrefixes) })
    }

    private fun doTest(fileContent: String, vararg expected: String) {
        doTestWithExtension(fileContent, "jsx", expected)
    }

    private fun doTestWithExtension(fileContent: String, extension: String, expected: Array<out String>) {
        myFixture.setCaresAboutInjection(false)
        val file = myFixture.configureByText("dummy.$extension", fileContent)
        myFixture.testHighlighting(true, false, false)
        Assert.assertEquals(expected.toList(), collectInjectedPsiContents(file))
    }

    private fun collectInjectedPsiContents(file: PsiFile): List<String> {
        return ContainerUtil.map(collectInjectedPsiFiles(file)) { element -> element.text }
    }

    private fun collectInjectedPsiFiles(file: PsiFile): List<PsiElement> {
        val result = LinkedHashSet<PsiFile>()
        PsiTreeUtil.processElements(file) {
            val host = it as? PsiLanguageInjectionHost
            if (host != null) {
                InjectedLanguageManager.getInstance(host.project)
                        .enumerate(host) { injectedPsi, _ -> result.add(injectedPsi) }
            }
            true
        }

        return ArrayList(result)
    }
}
