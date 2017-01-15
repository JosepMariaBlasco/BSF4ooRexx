package org.oorexx.ide.lexer;

import com.intellij.lexer.FlexAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;

public class OoRexxLexerAdapter extends FlexAdapter {
    public OoRexxLexerAdapter() {
        super(new OoRexxLexer((Reader) null));
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        super.start(buffer, 0, buffer.length(), initialState);
    }
}
