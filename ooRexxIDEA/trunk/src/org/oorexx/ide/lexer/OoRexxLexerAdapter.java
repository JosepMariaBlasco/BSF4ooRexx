package org.oorexx.ide.lexer;

import com.intellij.lexer.FlexAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;

public class OoRexxLexerAdapter extends FlexAdapter {
    public OoRexxLexerAdapter() {
        super(new OoRexxLexer(false));
    }
}
