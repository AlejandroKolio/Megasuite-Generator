package com.sperasoft.megasuite.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.actionSystem.impl.MenuItemPresentationFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.sperasoft.megasuite.icons.PluginIcons;
import com.sperasoft.megasuite.utils.NotificationUtils;
import com.sperasoft.megasuite.views.MegasuiteConsole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class MegasuiteGenerateAction extends BaseAction {

    public MegasuiteGenerateAction() {
        super(PluginIcons.Interest);
    }

    public MegasuiteGenerateAction(Icon icon) {
        super(icon);
    }

    @Override
    public void actionPerformed() {
        MegasuiteConsole.getInstance(currentProject).initAndActive();
    }

    private void showGearPopup(Component component, int x, int y) {
        ActionPopupMenu popupMenu =
                ((ActionManagerImpl) ActionManager.getInstance())
                        .createActionPopupMenu(ToolWindowContentUi.POPUP_PLACE,
                                createGearPopupGroup(), new MenuItemPresentationFactory(true));
        popupMenu.getComponent().show(component, x, y);
    }

    private DefaultActionGroup createGearPopupGroup() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new AnAction("Test") {

            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                NotificationUtils.infoNotification("Test");
            }
        });
        group.addSeparator();

        return group;
    }

    private class GearAction extends AnAction {
        GearAction() {
            Presentation presentation = getTemplatePresentation();
            presentation.setIcon(AllIcons.General.Gear);
            presentation.setHoveredIcon(AllIcons.General.GearHover);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            int x = 0;
            int y = 0;
            InputEvent inputEvent = e.getInputEvent();
            if (inputEvent instanceof MouseEvent) {
                x = ((MouseEvent)inputEvent).getX();
                y = ((MouseEvent)inputEvent).getY();
            }

            showGearPopup(inputEvent.getComponent(), x, y);
        }
    }
}
