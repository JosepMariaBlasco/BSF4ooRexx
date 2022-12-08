package org.oorexx.ide.intellij.completion.action;

import com.intellij.codeInsight.lookup.impl.actions.ChooseItemAction;

public class CompleteWithParentheseAction extends ChooseItemAction {
    public CompleteWithParentheseAction() {
        super(new Handler(false, '('));
    }
}
