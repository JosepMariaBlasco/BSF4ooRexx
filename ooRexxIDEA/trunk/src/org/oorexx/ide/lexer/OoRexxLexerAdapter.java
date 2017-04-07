package org.oorexx.ide.lexer;

import com.intellij.lexer.FlexAdapter;
import org.oorexx.ide.intellij.setting.OoRexxLanguageSettings;

public class OoRexxLexerAdapter extends FlexAdapter {

    public OoRexxLexerAdapter(OoRexxLanguageSettings ooRexxLanguageSettings) {
        super(new OoRexxLexer(ooRexxLanguageSettings));
    }

    public OoRexxLexerAdapter() {
        super(new OoRexxLexer());
    }
}
