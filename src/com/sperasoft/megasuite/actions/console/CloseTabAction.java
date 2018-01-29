package com.sperasoft.megasuite.actions.console;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.tabs.TabbedContentAction.CloseAction;
import org.jetbrains.annotations.NotNull;

/**
 * This action is used to close a content tab.
 */
public class CloseTabAction extends CloseAction {
    public CloseTabAction(@NotNull Content content) {
        super(content);
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabledAndVisible( myContent.isCloseable());//myManager.canCloseContents() &&
        presentation.setText(myManager.getCloseActionName());
        presentation.setIcon(AllIcons.Actions.Cancel);
    }
}
