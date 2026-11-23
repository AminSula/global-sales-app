package com.aminsula.pilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// accounts live in the app_user table.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class PilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PilotApplication.class, args);
    }
}
