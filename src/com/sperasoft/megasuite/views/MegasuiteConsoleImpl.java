package com.sperasoft.megasuite.views;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.impl.ConsoleState;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.psi.search.GlobalSearchScope;
import com.sperasoft.megasuite.utils.MegasuitePathUtil;
import com.sperasoft.megasuite.utils.NotificationUtils;
import org.jetbrains.annotations.NotNull;

public class MegasuiteConsoleImpl extends ConsoleViewImpl {

    private static int counter;

    // Rerun current command
    private class RerunAction extends AnAction {

        public RerunAction() {
            super("Rerun", "Rerun", AllIcons.Actions.Restart);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            reRun();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setVisible(myGeneralCommandLine != null);
            e.getPresentation().setEnabled(myGeneralCommandLine != null);
            if (displayName != null) {
                e.getPresentation().setText("Rerun '" + displayName + "'");
                e.getPresentation().setDescription("Rerun '" + displayName + "'");
            } else if (myGeneralCommandLine != null) {
                e.getPresentation().setText("Rerun '" + myGeneralCommandLine.getCommandLineString() + "'");
                e.getPresentation().setDescription("Rerun '" + myGeneralCommandLine.getCommandLineString() + "'");
            }
        }

        @Override
        public boolean isDumbAware() {
            return Registry.is("dumb.aware.run.configurations");
        }
    }

    private ProcessHandler myProcessHandler;
    private GeneralCommandLine myGeneralCommandLine;
    private StopProcessAction myStopProcessAction;
    private String displayName;

    public MegasuiteConsoleImpl(@NotNull Project project, boolean viewer) {
        super(project, viewer);
    }

    public MegasuiteConsoleImpl(@NotNull Project project,
                         @NotNull GlobalSearchScope searchScope,
                         boolean viewer,
                         boolean usePredefinedMessageFilter) {
        super(project, searchScope, viewer, usePredefinedMessageFilter);
    }

    protected MegasuiteConsoleImpl(@NotNull Project project,
                            @NotNull GlobalSearchScope searchScope,
                            boolean viewer,
                            @NotNull ConsoleState initialState,
                            boolean usePredefinedMessageFilter) {
        super(project, searchScope, viewer, initialState, usePredefinedMessageFilter);
    }

    public AnAction getReRunAction() {
        return new RerunAction();
    }

    public void setStopProcessAction(StopProcessAction myStopProcessAction) {
        this.myStopProcessAction = myStopProcessAction;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void reRun() {
        if (myGeneralCommandLine != null) {
            try {
                processCommandline(myGeneralCommandLine);
            } catch (ExecutionException e) {
                NotificationUtils.showNotification("Unable to run the commandline:" + e.getMessage(),
                        NotificationType.WARNING);
            }
        }
    }

    /**
     *
     * terminal
     *
     * @param cmd
     */
    public void executeCommand(String cmd, String workDirectory) {
        GeneralCommandLine commandLine = MegasuitePathUtil.createFullPathCommandLine(cmd, workDirectory);
        commandLine.setWorkDirectory(workDirectory);
        myGeneralCommandLine = commandLine;
        try {
            processCommandline(commandLine);
        } catch (ExecutionException e) {
            NotificationUtils.showNotification("Unable to run the commandline:" + e.getMessage(),
                    NotificationType.WARNING);
        }
    }

    /* process command line, will display a very simple console view and tool window */
    private void processCommandline(GeneralCommandLine commandLine) throws ExecutionException {
        if (myProcessHandler != null) {
            myProcessHandler.destroyProcess();
            clear();
            myProcessHandler = null;
        }

        final OSProcessHandler processHandler = new OSProcessHandler(commandLine);
        myProcessHandler = processHandler;
        myStopProcessAction.setProcessHandler(processHandler);

        ProcessTerminatedListener.attach(processHandler);

        processConsole(processHandler);

//        ApplicationManager.getApplication().invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                processConsole(processHandler);
//            }
//        });
    }

    private void processConsole(ProcessHandler processHandler) {
        attachToProcess(processHandler);
        processHandler.startNotify();
    }

    /**
     * Clean up process when close tab
     *
     * @since 1.0.6
     */
    public void dispose() {
        super.dispose();
        if (myProcessHandler != null && !myProcessHandler.isProcessTerminated()) {
            System.out.println("Terminate process of tab " + displayName + ", cmd:" + myGeneralCommandLine);
            myProcessHandler.destroyProcess();
            myProcessHandler = null;
        }
    }

}