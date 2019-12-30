package org.oorexx.ide.parser.standalone;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.lexer.OoRexxLexerAdapter;
import org.oorexx.ide.parser.OoRexxParserDefinition;


public class OoRexxStandaloneParserDefinition extends OoRexxParserDefinition {

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new OoRexxLexerAdapter();
    }
}
