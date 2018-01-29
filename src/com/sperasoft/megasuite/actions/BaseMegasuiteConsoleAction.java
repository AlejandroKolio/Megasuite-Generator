package com.sperasoft.megasuite.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.sperasoft.megasuite.views.MegasuiteConsole;

import javax.swing.*;

public abstract class BaseMegasuiteConsoleAction extends DumbAwareAction {
    protected MegasuiteConsole terminal;
    protected Project project;
    protected DataContext dataContext;

    public BaseMegasuiteConsoleAction(MegasuiteConsole terminal, String text, String description, Icon icon) {
        super(text, description, icon);
        this.terminal = terminal;
    }

    public BaseMegasuiteConsoleAction(MegasuiteConsole terminal, String text) {
        super(text);
        this.terminal = terminal;
    }

    public String getText() {
        return getTemplatePresentation().getText();
    }

    public Icon getIcon() {
        return getTemplatePresentation().getIcon();
    }

    public Project getProject() {
        return project;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        dataContext = e.getDataContext();
        project = e.getProject();

        doAction(e);
    }

    public abstract void doAction(AnActionEvent anActionEvent);
}
