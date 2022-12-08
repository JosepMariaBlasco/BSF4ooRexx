package org.oorexx.ide.intellij.completion;

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.lang.OoRexx;
import org.oorexx.ide.lang.OoRexxTypes;

public class BraceMatcher extends PairedBraceMatcherAdapter {
    public BraceMatcher() {
        super(new BraceHighlighterMatcher(), OoRexx.INSTANCE);
    }

    static class BraceHighlighterMatcher  implements PairedBraceMatcher {
        @Override
        public BracePair @NotNull [] getPairs() {
            return new BracePair[] {
                    new BracePair(OoRexxTypes.LPAREN, OoRexxTypes.RPAREN, false),
                    new BracePair(OoRexxTypes.LBRACK, OoRexxTypes.RBRACK, false),
                    new BracePair(OoRexxTypes.LCURLY, OoRexxTypes.RCURLY, false)
            } ;
        }

        @Override
        public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
            return true;
        }

        @Override
        public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
            return openingBraceOffset;
        }
    }
}


