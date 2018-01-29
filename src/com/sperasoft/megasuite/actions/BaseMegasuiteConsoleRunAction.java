package com.sperasoft.megasuite.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.sperasoft.megasuite.views.MegasuiteConsole;

import javax.swing.*;

public abstract class BaseMegasuiteConsoleRunAction extends BaseMegasuiteConsoleAction {
    public BaseMegasuiteConsoleRunAction(MegasuiteConsole terminal, String text) {
        super(terminal, text);
    }

    public BaseMegasuiteConsoleRunAction(MegasuiteConsole terminal, String text, String description, Icon icon) {
        super(terminal, text, description, icon);
    }

    @Override
    public void doAction(AnActionEvent anActionEvent) {
        if(beforeAction()) {
            terminal.runTab(command(), null, getText(), getIcon());
            afterAction();
        }
    }

    // Some action before runTab commands, eg mkdir through API or shell
    public boolean beforeAction() {
        return true;
    }

    // Some action after runTab commands, eg clean dir through API or shell
    public void afterAction() {
    }

    // single line command to run
    protected abstract String command();
}
