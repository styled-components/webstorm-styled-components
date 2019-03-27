import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.styledComponents.CustomInjectionsConfiguration
import com.intellij.util.containers.ContainerUtil
import org.junit.Assert

class InjectionTest : LightPlatformCodeInsightFixtureTestCase() {
    
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
                "`;","div {\n" +
                        "    /* Adapt the colours based on primary prop */\n" +
                        "   background: EXTERNAL_FRAGMENT;\n" +
                        "   color: EXTERNAL_FRAGMENT;\n" +
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
                "div {EXTERNAL_FRAGMENT:red}",
                "div {color:EXTERNAL_FRAGMENT}")
    }

    fun testWithCustomInjectionMediaQuery() {
        setCustomInjectionsConfiguration("media")
        doTest("const Container = styled.div`\n" +
                "  color: #333;\n" +
                "  \${media.desktop `padding: 0 20px;` };\n" +
                "`", "div {\n" +
                "  color: #333;\n" +
                "  EXTERNAL_FRAGMENT;\n" +
                "}", "div {padding: 0 20px;}")
    }
    
    fun testCustomInjectionWithComplexTag() {
        setCustomInjectionsConfiguration("bp")
        doTest("const Container = styled.div`\n" +
                "  color: #333;\n" +
                "  \${bp(media.tablet)`padding: 0 20px;` }\n" +
                "`", "div {\n" +
                "  color: #333;\n" +
                "  EXTERNAL_FRAGMENT\n" +
                "}", "div {padding: 0 20px;}")
    }

    private fun setCustomInjectionsConfiguration(vararg prefixes: String) {
        val configuration = CustomInjectionsConfiguration.instance(myFixture.project)
        val previousPrefixes = configuration.getTagPrefixes()
        configuration.setTagPrefixes(arrayOf(*prefixes))
        Disposer.register(myFixture.testRootDisposable, Disposable { configuration.setTagPrefixes(previousPrefixes) })
    }

    private fun doTest(fileContent: String, vararg expected: String) {
        myFixture.setCaresAboutInjection(true)
        val file = myFixture.configureByText("dummy.es6", fileContent)
        myFixture.testHighlighting(true, false, false)
        Assert.assertEquals(expected.toList(), collectInjectedPsiContents(file))
    }

    private fun collectInjectedPsiContents(file: PsiFile): List<String> {
        return ContainerUtil.map(collectInjectedPsiFiles(file)) { element -> element.text }
    }

    private fun collectInjectedPsiFiles(file: PsiFile): List<PsiElement> {
        val result = ContainerUtil.newLinkedHashSet<PsiFile>()
        PsiTreeUtil.processElements(file) {
            val host = it as? PsiLanguageInjectionHost
            if (host != null) {
                InjectedLanguageManager.getInstance(host.project)
                        .enumerate(host) { injectedPsi, _ -> result.add(injectedPsi) }
            }
            true
        }

        return ContainerUtil.newArrayList<PsiElement>(result)
    }
}
