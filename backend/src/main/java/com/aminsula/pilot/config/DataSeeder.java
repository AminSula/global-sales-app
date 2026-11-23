package com.aminsula.pilot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.aminsula.pilot.user.AppUser;
import com.aminsula.pilot.user.AppUserRepository;
import com.aminsula.pilot.user.Role;

/**
 * Creates the three pilot accounts the first time the app starts on an empty database.
 *
 * The first passwords come from the environment variables ADMIN_PASSWORD and AGENT_PASSWORD.
 * Without them every account gets the demo password 123456, which is only acceptable on a
 * test machine. They are read once: after the accounts exist, changing the variables does nothing.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String agentPassword;

    public DataSeeder(AppUserRepository users,
                      PasswordEncoder passwordEncoder,
                      @Value("${ADMIN_PASSWORD:123456}") String adminPassword,
                      @Value("${AGENT_PASSWORD:123456}") String agentPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.agentPassword = agentPassword;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        create("admin", adminPassword, "Administrator", Role.ADMIN);
        create("agent1", agentPassword, "Agent 1", Role.AGENT);
        create("agent2", agentPassword, "Agent 2", Role.AGENT);
    }

    private void create(String username, String password, String fullName, Role role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        users.save(user);
    }
}
