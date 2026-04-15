package com.cauecalil.personalfinance.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.desktop")
public record DesktopProperties(
        boolean openBrowserOnStartup,
        @NotBlank String startupUrl
) {}
