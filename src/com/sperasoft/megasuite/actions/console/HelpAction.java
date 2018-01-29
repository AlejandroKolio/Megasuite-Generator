package com.sperasoft.megasuite.actions.console;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.SystemInfo;
import com.sperasoft.megasuite.actions.BaseMegasuiteConsoleAction;
import com.sperasoft.megasuite.views.MegasuiteConsole;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * Open help message.
 */
public class HelpAction extends BaseMegasuiteConsoleAction {
    public HelpAction(MegasuiteConsole terminal) {
        super(terminal, "Help", "Show Megasuite Console docs online", AllIcons.Actions.Help);
    }

    @Override
    public void doAction(AnActionEvent anActionEvent) {
        openUrl("https://github.com");
    }

    private static void openUrl(String url) {
        if (SystemInfo.isWindows) {
            try {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                URI uri = new URI(url);
                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                }
                if (desktop != null)
                    desktop.browse(uri);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String uriEncode(String in) {
        String out = new String();
        for (char ch : in.toCharArray()) {
            out += Character.isLetterOrDigit(ch) ? ch : String.format("%%%02X", (int)ch);
        }
        return out;
    }
}