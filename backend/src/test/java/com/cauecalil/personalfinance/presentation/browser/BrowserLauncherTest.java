package com.cauecalil.personalfinance.presentation.browser;

import com.cauecalil.personalfinance.config.DesktopProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrowserLauncherTest {
    @Test
    void shouldNotTryToOpenBrowserWhenDisabled() {
        DesktopProperties properties = new DesktopProperties(false, "::invalid-uri::");
        BrowserLauncher launcher = new BrowserLauncher(properties);

        assertDoesNotThrow(launcher::onApplicationReady);
    }

    @Test
    void shouldFailFastWhenStartupUrlIsInvalidAndBrowserLaunchIsEnabled() {
        DesktopProperties properties = new DesktopProperties(true, "::invalid-uri::");
        BrowserLauncher launcher = new BrowserLauncher(properties);

        assertThrows(IllegalStateException.class, launcher::onApplicationReady);
    }
}
