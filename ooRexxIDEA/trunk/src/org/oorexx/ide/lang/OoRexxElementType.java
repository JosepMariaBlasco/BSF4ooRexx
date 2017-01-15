package org.oorexx.ide.lang;

import com.intellij.psi.tree.IElementType;

public class OoRexxElementType extends IElementType {
  public OoRexxElementType(String debug_description) {
    super(debug_description, OoRexx.INSTANCE);
  }
}
