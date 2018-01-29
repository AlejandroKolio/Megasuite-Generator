package com.sperasoft.megasuite.utils;

import com.google.gson.Gson;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class MegasuitePathUtil {
    public static final String PACKAGE_JSON = "package.json";
    static String GRADLE_FILE = "build.gradle";
    static String MS_CONSOLE_FILE = ".idea" + File.separator + ".rnconsole";

    /**
     * Get the real megasuite project root path.
     *
     * @param project
     * @return
     */
    private static String getMegasuiteProjectRootPathFromConfig(Project project) {
        String path = project.getBasePath();
        File file = new File(path, MS_CONSOLE_FILE);
        if (file.exists()) {
            String p = parseCurrentPathFromMegasuiteConsoleJsonFile(file);
            if (p != null) {
                return new File(path, p).getAbsolutePath();
            }
            return null;
        } else {
            return null;
        }
    }

    public static String parseCurrentPathFromMegasuiteConsoleJsonFile(File f) {
        try {
            Map m = new Gson().fromJson(new FileReader(f), Map.class);
            return (String) m.get("currentPath");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getMegasuiteProjectPath(Project project) {
        String realPath = getMegasuiteProjectRootPathFromConfig(project);
        if (realPath != null) {
            System.out.println("realPath=" + realPath);
            return realPath;
        }

        String inputDir = project.getBasePath();
        File file = new File(inputDir, PACKAGE_JSON);
        if (file.exists()) {
            return inputDir;
        } else {
            file = new File(inputDir, ".." + File.separatorChar + PACKAGE_JSON);
            if (file.exists()) {
                return inputDir + File.separatorChar + "..";
            }
        }

        return null;
    }

    /**
     * Get the full path of an exe file by the IDEA platform.
     * On Mac sys, PATH might will not be directly access by the IDE code,
     * eg: Android Studio Runtime.exec('adb') will throw a no file or directory exception.
     *
     * @param exeName
     * @return
     */
    public static String getExecuteFileFullPath(String exeName) {
        String fullPath = exeName;

        if (OSUtils.isWindows()) {
            if (!exeName.endsWith(".exe")) {
                // first try exe
                fullPath = getExecuteFullPathSingle(exeName + ".exe");
                if (fullPath != null) {
                    return fullPath;
                }

                if (!exeName.endsWith(".cmd")) {
                    fullPath = getExecuteFullPathSingle(exeName + ".cmd");
                    if (fullPath != null) {
                        return fullPath;
                    }
                }

                if (!exeName.endsWith(".bat")) {
                    fullPath = getExecuteFullPathSingle(exeName + ".bat");
                    if (fullPath != null) {
                        return fullPath;
                    }
                }
            }
        }
        fullPath = getExecuteFullPathSingle(exeName);

        return fullPath;
    }

    public static String getExecuteFullPathSingle(String exeName) {
        List<File> fromPath = PathEnvironmentVariableUtil.findAllExeFilesInPath(exeName);
        if (fromPath != null && fromPath.size() > 0) {
            return fromPath.get(0).toString();
        }
        return null;
    }

    public static String createAdbCommand(String args) {
        String adbFullPath = getExecuteFileFullPath("adb");
        if (adbFullPath != null) {
            return adbFullPath + " " + args;
        }
        return "adb" + " " + args;
    }

    public static String createCommand(String exe, String args) {
        String adbFullPath = getExecuteFileFullPath(exe);
        if (adbFullPath != null) {
            return adbFullPath + " " + args;
        }
        return exe + " " + args;
    }


    public static GeneralCommandLine cmdToGeneralCommandLine(String cmd) {
        GeneralCommandLine commandLine = new GeneralCommandLine(cmd.split(" "));
        commandLine.setCharset(Charset.forName("UTF-8"));
        return commandLine;
    }

    /**
     * Create full path command line.
     *
     * @param shell
     * @param workDirectory - only used on windows for gradlew.bat
     * @return GeneralCommandLine
     */
    @NotNull
    public static GeneralCommandLine createFullPathCommandLine(String shell, @Nullable String workDirectory) {
        String[] cmds = shell.split(" ");
        String exeFullPath;
        GeneralCommandLine commandLine = null;
        if (cmds.length > 1) {
            String exePath = cmds[0];

            List<String> cmdList = new ArrayList<>();
            cmdList.addAll(Arrays.asList(cmds));
            exeFullPath = getExecuteFileFullPath(exePath);
            if (exeFullPath == null) {
                exeFullPath = exePath;
            }

            if (OSUtils.isWindows()) {
                if (exeFullPath.equals("gradlew.bat")) {
                    exeFullPath = workDirectory + File.separator + exeFullPath;
                }
            }

            cmdList.remove(0);
            cmdList.add(0, exeFullPath);

            commandLine = new GeneralCommandLine(cmdList);

        } else {
            exeFullPath = getExecuteFileFullPath(shell);
            if (exeFullPath == null) {
                exeFullPath = shell;
            }

            commandLine = new GeneralCommandLine(exeFullPath);
        }

        commandLine.setCharset(Charset.forName("UTF-8"));

        return commandLine;
    }
}