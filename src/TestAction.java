import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;

import java.util.Collection;

/**
 * Created by deadlock on 8/29/17.
 */
public class TestAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        // Get the selection to generate the injectable name.
        String caret = e.getData(PlatformDataKeys.FILE_TEXT);
        String injectableWithUnderscores =
                "_" + caret + "_";

//        this.findSCBlocks(e.getData(PlatformDataKeys.PSI_FILE));
    }


//    PsiElement findSCBlocks(PsiFile file) {
//        Collection<JSStringTemplateExpression> templates = PsiTreeUtil.findChildrenOfType(file, JSStringTemplateExpression.class);
//
//        for ( PsiElement template: templates ) {
//            Collection<PsiElement> refs = PsiTreeUtil.findChildrenOfAnyType(template, JSReferenceExpression.class);
//            if(refs.size() == 0) {
//                templates.remove(template);
//            }
//        }
//
//        for( PsiElement template: templates) {
//            //Get css block
//            Collection<PsiElement> scBlocks = PsiTreeUtil.findChildrenOfType(template, PsiElement.class);
//            System.out.println("Searching for scBlocks");
//            for(PsiElement e : scBlocks) {
//                if(e.getNode().getElementType() == JSTokenTypes.STRING_TEMPLATE_PART) {
//                    System.out.println(e.getText());
//                }
//            }
//        }
//
//        return null;
//    }
}
