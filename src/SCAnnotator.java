import com.intellij.lang.annotation.*;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Collection;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;
/**
 * Created by deadlock on 8/30/17.
 */
public class SCAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiLiteralExpression) {
            PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
            this.findSCBlocks(element);
        }
    }

    PsiElement findSCBlocks(PsiElement file) {
        Collection<JSStringTemplateExpression> templates = PsiTreeUtil.findChildrenOfType(file, JSStringTemplateExpression.class);

        for ( PsiElement template: templates ) {
            Collection<PsiElement> refs = PsiTreeUtil.findChildrenOfAnyType(template, JSReferenceExpression.class);
            if(refs.size() == 0) {
                templates.remove(template);
            }
        }

        for( PsiElement template: templates) {
            //Get css block
            Collection<PsiElement> scBlocks = PsiTreeUtil.findChildrenOfType(template, PsiElement.class);
            System.out.println("Searching for scBlocks");
            for(PsiElement e : scBlocks) {
                if(e.getNode().getElementType() == JSTokenTypes.STRING_TEMPLATE_PART) {
                    System.out.println(e.getText());
                }
            }
        }



        return null;
    }
}
