package org.oorexx.ide.lexer.token;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.lang.OoRexx;

public class OoRexxTokenType extends IElementType {

    public OoRexxTokenType(@NotNull @NonNls String debugName) {
        super(debugName, OoRexx.INSTANCE);
    }

    @Override
    public String toString() {
        return getTokenName().toUpperCase();
    }

    public String getTokenName(){
        return super.toString();
    }
}
