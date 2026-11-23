package com.aminsula.pilot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Prints a clear warning at startup when the app still runs with settings that are only
 * meant for a developer's own machine. It never stops the app.
 */
@Component
public class StartupChecks implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupChecks.class);

    private final String jwtSecret;

    public StartupChecks(@Value("${app.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Override
    public void run(String... args) {
        if (jwtSecret.startsWith("dev-only-secret")) {
            log.warn("JWT_SECRET is not set: login tokens are signed with the built-in development secret. "
                    + "Set the JWT_SECRET environment variable (32+ characters) before anyone outside this machine uses the app.");
        }
    }
}
