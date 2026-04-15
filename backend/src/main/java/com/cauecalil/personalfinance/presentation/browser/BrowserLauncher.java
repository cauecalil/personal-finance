package com.cauecalil.personalfinance.presentation.browser;

import com.cauecalil.personalfinance.config.DesktopProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrowserLauncher {
    private final DesktopProperties desktopProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!desktopProperties.openBrowserOnStartup()) {
            return;
        }

        URI startupUri = parseStartupUri(desktopProperties.startupUrl());

        if (tryOpenWithDesktop(startupUri) || tryOpenWithSystemCommand(startupUri)) {
            return;
        }

        log.warn("Could not open browser automatically. Open this URL manually: {}", startupUri);
    }

    private URI parseStartupUri(String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid app.desktop.startup-url: " + value, e);
        }
    }

    private boolean tryOpenWithDesktop(URI startupUri) {
        if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
            return false;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            return false;
        }

        try {
            desktop.browse(startupUri);
            return true;
        } catch (IOException e) {
            log.warn("Desktop browse failed for {}. Trying OS fallback.", startupUri, e);
            return false;
        }
    }

    private boolean tryOpenWithSystemCommand(URI startupUri) {
        List<String> command = detectCommand(startupUri);
        if (command.isEmpty()) {
            return false;
        }

        try {
            new ProcessBuilder(command).start();
            return true;
        } catch (IOException e) {
            log.warn("OS fallback command failed for {}: {}", startupUri, command, e);
            return false;
        }
    }

    private List<String> detectCommand(URI startupUri) {
        String url = startupUri.toString();
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            return List.of("cmd", "/c", "start", "", url);
        }
        if (osName.contains("mac")) {
            return List.of("open", url);
        }
        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return List.of("xdg-open", url);
        }
        return List.of();
    }
}
