package com.sperasoft.megasuite.views;

import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.filters.BrowserHyperlinkInfo;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentImpl;
import com.sperasoft.megasuite.actions.console.CloseTabAction;
import com.sperasoft.megasuite.actions.console.HelpAction;
import com.sperasoft.megasuite.actions.console.StartSession;
import com.sperasoft.megasuite.icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

public class MegasuiteConsole implements FocusListener, ProjectComponent {

    private static int counter;
    private Project myProject;

    public static MegasuiteConsole getInstance(Project project) {
        return project.getComponent(MegasuiteConsole.class);
    }

    public MegasuiteConsole(Project project) {
        this.myProject = project;
    }

    private ToolWindow getToolWindow() {
        return ToolWindowManager.getInstance(myProject).getToolWindow(MegasuiteToolWindowFactory.TOOL_WINDOW_ID);
    }

    public void initAndActive() {
        ToolWindow toolWindow = getToolWindow();
        if (!toolWindow.isActive()) {
            toolWindow.activate(null);
        }
    }

    public void initTerminal(final ToolWindow toolWindow) {
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setStripeTitle("Megasuite Console");
        toolWindow.setIcon(PluginIcons.Interest);
        createConsoleTabContent(toolWindow, true, "Welcome", null);

        toolWindow.setShowStripeButton(true);
        ((ToolWindowManagerEx) ToolWindowManager.getInstance(this.myProject))
                .addToolWindowManagerListener(new ToolWindowManagerListener() {
                    @Override
                    public void toolWindowRegistered(@NotNull String s) {
                    }

                    @Override
                    public void stateChanged() {
                        ToolWindow window = ToolWindowManager.getInstance(myProject)
                                .getToolWindow(MegasuiteToolWindowFactory.TOOL_WINDOW_ID);
                        if (window != null) {
                            boolean visible = window.isVisible();
                            if (visible && toolWindow.getContentManager().getContentCount() == 0) {
                                initTerminal(window);
                            }
                        }
                    }
                });
        toolWindow.show(null);
    }

    /**
     * Megasuite Console.
     *
     * @param displayName - the tab's display name must be unique.
     * @param icon        - used to set a tab icon, not used for search
     * @return
     */
    public MegasuiteConsoleImpl getMegasuiteConsole(String displayName, Icon icon) {
        ToolWindow window = ToolWindowManager.getInstance(myProject).getToolWindow(MegasuiteToolWindowFactory.TOOL_WINDOW_ID);

        String s = displayName + " " + counter;
        if (window != null) {
            Content existingContent = createConsoleTabContent(window, false, s, icon);
            if (existingContent != null) {
                final JComponent existingComponent = existingContent.getComponent();

                if (existingComponent instanceof SimpleToolWindowPanel) {
                    JComponent component = ((SimpleToolWindowPanel) existingComponent).getContent();
                    if (component instanceof MegasuiteConsoleImpl) {
                        MegasuiteConsoleImpl megasuiteConsole = (MegasuiteConsoleImpl) component;
                        counter++;
                        return megasuiteConsole;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Create a console panel
     *
     * @param toolWindow
     * @return
     */
    private Content createConsoleTabContent(@NotNull final ToolWindow toolWindow, boolean firstInit, String displayName, Icon icon) {
        final ContentManager contentManager = toolWindow.getContentManager();
        final Content existingContent = contentManager.findContent(displayName);
        if (existingContent != null) {
            contentManager.setSelectedContent(existingContent);
            return existingContent;
        }

        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true);
        ContentImpl content = new ContentImpl(panel, displayName, true);
        content.setCloseable(true);

        MegasuiteConsoleImpl consoleView = new MegasuiteConsoleImpl(myProject, true);
        consoleView.setDisplayName(displayName);
        content.setDisposer(consoleView);

        if (icon != null) {
            content.setIcon(icon);
            content.setPopupIcon(icon);
            content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);// Set to show tab icon
        }

        if (firstInit) {
            content.setCloseable(false);
            content.setDisplayName("Welcome");
            content.setDescription("");
            content.setIcon(PluginIcons.Interest);
            content.setPopupIcon(PluginIcons.Interest);
            content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
            consoleView.print(
                    "Welcome to Megasuite Console, now please click one button on top toolbar to start.\n",
                    ConsoleViewContentType.SYSTEM_OUTPUT);
            consoleView.print(
                    "Click here for more info and issue, suggestion:\n",
                    ConsoleViewContentType.NORMAL_OUTPUT);
            consoleView.printHyperlink("https://github.com/",
                    new BrowserHyperlinkInfo("https://github.com/"));
        }

        panel.setContent(consoleView.getComponent());
        panel.addFocusListener(this);


        // main toolbars
        DefaultActionGroup group = new DefaultActionGroup();
        ActionToolbar toolbarWest = ActionManager.getInstance().createActionToolbar("unknown", (ActionGroup) group, false);
        toolbarWest.setTargetComponent(consoleView.getComponent());
        panel.setToolbar(toolbarWest.getComponent(), true);

        // welcome page don't show console action buttons
        if (!firstInit) {
            // Create left console and normal toolbars
            DefaultActionGroup toolbarActions = new DefaultActionGroup();
            AnAction[] consoleActions = consoleView.createConsoleActions();// 必须在 consoleView.getComponent() 调用后组件真正初始化之后调用
            // resort console actions to move scroll to end and clear to top
            List<AnAction> resortActions = new ArrayList<>();
            if (consoleActions != null) {
                for (AnAction action : consoleActions) {
                    if (action instanceof ScrollToTheEndToolbarAction || action instanceof ConsoleViewImpl.ClearAllAction) {
                        resortActions.add(action);
                    }
                }

                for (AnAction action : consoleActions) {
                    if (!(action instanceof ScrollToTheEndToolbarAction || action instanceof ConsoleViewImpl.ClearAllAction)) {
                        resortActions.add(action);
                    }
                }
            }

            // Rerun current command
/*            group.add(consoleView.getReRunAction());
            group.addSeparator();*/
            // Stop and close tab
            StopProcessAction stopProcessAction = new StopProcessAction("Stop process", "Stop process", null);
            consoleView.setStopProcessAction(stopProcessAction);
            group.add(stopProcessAction);

            content.setManager(toolWindow.getContentManager());
            group.add(new StartSession(this));
            group.add(new CloseTabAction(content));
            group.addSeparator();
            // Built in console action
            group.addAll(resortActions.toArray(new AnAction[0]));

            ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("unknown", (ActionGroup) toolbarActions, false);
            toolbar.setTargetComponent(consoleView.getComponent());
            panel.setToolbar(toolbar.getComponent(), false);
        }

        // General
        group.add(new StartSession(this));
        group.addSeparator();
        group.add(new HelpAction(this));

        content.setPreferredFocusableComponent(consoleView.getComponent());

        toolWindow.getContentManager().addContent(content);
        contentManager.setSelectedContent(content);

        return content;
    }


    /**
     * @param command
     */
    public void runTab(String command, String workDirectory, String displayName, Icon icon) {
        MegasuiteConsoleImpl megasuiteConsole = getMegasuiteConsole(displayName, icon);
        if (megasuiteConsole != null) {
            megasuiteConsole.executeCommand(command, workDirectory);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(myProject)
                .getToolWindow(MegasuiteToolWindowFactory.TOOL_WINDOW_ID);
        if (toolWindow != null) {
            try {
                ContentManager contentManager = toolWindow.getContentManager();
                JComponent component = contentManager.getSelectedContent().getComponent();
                if (component != null) {
                    component.requestFocusInWindow();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "MegasuiteGeneratorConsole";
    }

}