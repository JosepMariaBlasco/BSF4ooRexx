package org.oorexx.ide.lexer;

import com.intellij.lexer.FlexAdapter;

public class OoRexxLexerAdapter extends FlexAdapter {

    public OoRexxLexerAdapter() {
        super(new OoRexxLexer());
    }
}
