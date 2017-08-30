import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.ecmascript6.ES6ElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

/**
 * Created by deadlock on 8/30/17.
 */
public class SCInjector implements LanguageInjector {
    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost psiLanguageInjectionHost, @NotNull InjectedLanguagePlaces injectedLanguagePlaces) {
        PsiElement element = psiLanguageInjectionHost;

        if (element.getParent().getNode().getElementType() != ES6ElementTypes.TAGGED_TEMPLATE_EXPRESSION) {
            return;
        }

        String parentExpressionText = element.getParent().getFirstChild().getText();
        if (!parentExpressionText.startsWith("styled") && !parentExpressionText.startsWith("keyframes") && !parentExpressionText.endsWith("extend")) {
            return;
        }

        Collection<PsiElement> refs = PsiTreeUtil.findChildrenOfAnyType(element, PsiElement.class);
        for (PsiElement c : refs) {
            if (c.getNode().getElementType() == JSTokenTypes.STRING_TEMPLATE_PART) {
                TextRange range = new TextRange(c.getStartOffsetInParent(), c.getStartOffsetInParent() + c.getTextLength());
                System.out.println(c.getText());
                String prefix = c.getText().trim().indexOf(":") < c.getText().trim().indexOf(";") &&
                        c.getText().trim().indexOf(":") != -1 &&
                        c.getText().trim().indexOf(";") != -1 ? "sel {" : "sel { fakeprop: initial ";

                if (c.getText().trim().endsWith(":")) {
                    prefix = "sel {";
                }
                String suffix = c.getText().trim().endsWith(";") ? "}" : "initial; }";
                injectedLanguagePlaces.addPlace(CSSLanguage.findLanguageByID("SCSS"), range, prefix, suffix);
            }
        }
    }

    Collection<PsiElement> findSCBlocks(PsiElement file) {
        Collection<JSStringTemplateExpression> templates = PsiTreeUtil.findChildrenOfType(file, JSStringTemplateExpression.class);
        Collection<PsiElement> blocks = new ArrayList<PsiElement>();
        for (PsiElement template : templates) {
            Collection<PsiElement> refs = PsiTreeUtil.findChildrenOfAnyType(template, JSReferenceExpression.class);
            if (refs.size() == 0) {
                templates.remove(template);
            }
        }

        for (PsiElement template : templates) {
            //Get css block
            Collection<PsiElement> scBlocks = PsiTreeUtil.findChildrenOfType(template, PsiElement.class);
            for (PsiElement e : scBlocks) {
                if (e.getNode().getElementType() == JSTokenTypes.STRING_TEMPLATE_PART) {
                    blocks.add(e);
                }
            }
        }


        return blocks;
    }
}
