package org.oorexx.ide.intellij.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.lang.OoRexx;

public class OoRexxCompletionService extends BaseCompletionService {

    private final ThreadLocal<Boolean> isOoRexxLanguage = new ThreadLocal<>();

    @Override
    protected String suggestPrefix(CompletionParameters parameters) {
        boolean isOoRexx = isOoRexx(parameters);
        isOoRexxLanguage.set(isOoRexx);
        if(!isOoRexx){
            return super.suggestPrefix(parameters);
        }

        PsiElement position = parameters.getPosition();
        Editor editor = parameters.getEditor();
        int textOffset = position.getTextOffset();
        String text = editor.getDocument().getText(new TextRange(Math.max(0, textOffset-20), textOffset));

      String prefix = "";

        if(position.getText() != null) {
            if(text.endsWith("::")) {
                prefix = "::";
            }
            else if(text.endsWith(":")) {
                prefix = ":";
            }else {
                prefix = switch (""+position.getText().replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "")) {
                    case "." -> ".";
                    case "?" -> "?";
                    case ":" -> ":";
                    default -> "";
                };
            }
        }

        return prefix + super.suggestPrefix(parameters);
    }

    private static boolean isOoRexx(CompletionParameters parameters) {
        return parameters.getOriginalFile().getLanguage().getClass().equals(OoRexx.class);
    }


    @Override
    protected @NotNull PrefixMatcher createMatcher(String prefix, boolean typoTolerant) {
        if(Boolean.FALSE.equals(isOoRexxLanguage.get())){
            return super.createMatcher(prefix, typoTolerant);
        }
        return new PlainPrefixMatcher(prefix);
    }
}
