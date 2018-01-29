package com.sperasoft.megasuite.actions.console;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.sperasoft.megasuite.actions.BaseMegasuiteConsoleRunAction;
import com.sperasoft.megasuite.views.MegasuiteConsole;
import com.sperasoft.megasuite.views.MegasuiteConsoleImpl;

public class StartSession extends BaseMegasuiteConsoleRunAction {
    public StartSession(MegasuiteConsole terminal) {
        super(terminal, "Session", "Start Megasuite Console new tab", AllIcons.General.Add);
    }

    @Override
    protected String command() {
        return "adb shell input keyevent 82";
    }
}
