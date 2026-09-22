package com.noc.authservice.config;

import com.noc.authservice.entity.EngineerProfile;
import com.noc.authservice.entity.EngineerSkill;
import com.noc.authservice.entity.Role;
import com.noc.authservice.entity.User;
import com.noc.authservice.repository.EngineerProfileRepository;
import com.noc.authservice.repository.EngineerSkillRepository;
import com.noc.authservice.repository.RoleRepository;
import com.noc.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bootstraps a default dataset on first run only (skipped if any
 * role already exists) so there is a working set of dummy users:
 * - Admin (admin / Admin@123)
 * - NOC Operator (operator1 / Operator@123)
 * - Engineers (engineer1 / Engineer@123, engineer2 / Engineer@123)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EngineerProfileRepository engineerProfileRepository;
    private final EngineerSkillRepository engineerSkillRepository;
    private final com.noc.authservice.repository.RegistrationRequestRepository registrationRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role admin = roleRepository.findByNameIgnoreCase("ADMIN")
                .orElseGet(() -> seedRole("ADMIN", "System owner — manages users, approvals, specialization and SLA config"));
        Role nocOperator = roleRepository.findByNameIgnoreCase("NOC_OPERATOR")
                .orElseGet(() -> seedRole("NOC_OPERATOR", "Monitors alarms/incidents and verifies Engineer-resolved work"));
        Role engineer = roleRepository.findByNameIgnoreCase("ENGINEER")
                .orElseGet(() -> seedRole("ENGINEER", "Hands-on fixer — works incidents assigned to them"));

        // 1. Admin
        seedOrUpdateUser("admin", "Admin@123", "admin@nocpulse.com", "System Administrator", admin.getId());

        // 2. NOC Operators
        seedOrUpdateUser("operator1", "Operator@123", "operator1@nocpulse.com", "NOC Operator Alex", nocOperator.getId());
        seedOrUpdateUser("noc", "Operator@123", "noc@nocpulse.com", "NOC Operator", nocOperator.getId());
        seedOrUpdateUser("test_noc", "Operator@123", "test_noc@nocpulse.com", "Test NOC Operator", nocOperator.getId());

        // 3. Engineer 1 (Routing & Switching)
        seedOrUpdateEngineer("engineer1", "Engineer@123", "engineer1@nocpulse.com", "Sarah Jenkins",
                engineer.getId(), "Routing & Switching", 10, List.of("BGP", "OSPF", "Cisco", "MPLS"));

        // 4. Engineer 2 (Firewall & Security)
        seedOrUpdateEngineer("engineer2", "Engineer@123", "engineer2@nocpulse.com", "David Miller",
                engineer.getId(), "Firewall & Security", 10, List.of("VPN", "Palo Alto", "Fortinet", "Check Point"));

        // 5. Engineer 3 (Cloud Networking)
        seedOrUpdateEngineer("engineer3", "Engineer@123", "engineer3@nocpulse.com", "Emily Watson",
                engineer.getId(), "Cloud Networking", 10, List.of("AWS DirectConnect", "Azure ExpressRoute", "Terraform", "SD-WAN"));

        // 6. Engineer 4 (Wireless & Mobility)
        seedOrUpdateEngineer("engineer4", "Engineer@123", "engineer4@nocpulse.com", "Michael Chang",
                engineer.getId(), "Wireless & Mobility", 10, List.of("Wi-Fi 6", "Aruba", "Cisco DNA", "802.1X"));

        // 7. Engineer 5 (Data Center Infrastructure)
        seedOrUpdateEngineer("engineer5", "Engineer@123", "engineer5@nocpulse.com", "Robert Garcia",
                engineer.getId(), "Data Center Infrastructure", 10, List.of("Nexus", "VXLAN", "BGP EVPN", "ACI"));

        // 8. Sample Pending NOC Operator Registration Request
        if (!userRepository.existsByUsernameIgnoreCase("pending_noc")) {
            User pendingNoc = User.builder()
                    .username("pending_noc")
                    .password(passwordEncoder.encode("Operator@123"))
                    .email("pending_noc@nocpulse.com")
                    .fullName("John Doe (Pending)")
                    .roleId(nocOperator.getId())
                    .enabled(false)
                    .build();
            User savedPendingNoc = userRepository.save(pendingNoc);
            registrationRequestRepository.save(com.noc.authservice.entity.RegistrationRequest.builder()
                    .userId(savedPendingNoc.getId())
                    .status("PENDING")
                    .build());
        }

        // 9. Sample Pending Engineer Registration Request
        if (!userRepository.existsByUsernameIgnoreCase("pending_eng")) {
            User pendingEng = User.builder()
                    .username("pending_eng")
                    .password(passwordEncoder.encode("Engineer@123"))
                    .email("pending_eng@nocpulse.com")
                    .fullName("Lisa Ray (Pending)")
                    .roleId(engineer.getId())
                    .enabled(false)
                    .build();
            User savedPendingEng = userRepository.save(pendingEng);
            engineerProfileRepository.save(EngineerProfile.builder()
                    .userId(savedPendingEng.getId())
                    .primarySpecialization("Cloud Networking")
                    .activeTaskLimit(10)
                    .build());
            registrationRequestRepository.save(com.noc.authservice.entity.RegistrationRequest.builder()
                    .userId(savedPendingEng.getId())
                    .status("PENDING")
                    .build());
        }



        log.info("==================================================================");
        log.info(" auth-service: Seeded default active engineers for auto-assignment.");
        log.info(" Engineer1: engineer1 / Engineer@123 (Routing & Switching)");
        log.info(" Engineer2: engineer2 / Engineer@123 (Firewall & Security)");
        log.info(" Engineer3: engineer3 / Engineer@123 (Cloud Networking)");
        log.info(" Engineer4: engineer4 / Engineer@123 (Wireless & Mobility)");
        log.info(" Engineer5: engineer5 / Engineer@123 (Data Center Infrastructure)");
        log.info("==================================================================");
    }

    private void seedOrUpdateEngineer(String username, String password, String email, String fullName,
                                      Long roleId, String specialization, int taskLimit, List<String> skills) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElseGet(() ->
                userRepository.save(User.builder()
                        .username(username)
                        .password(passwordEncoder.encode(password))
                        .email(email)
                        .fullName(fullName)
                        .roleId(roleId)
                        .enabled(true)
                        .build())
        );

        // Ensure user is enabled
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            user.setEnabled(true);
            userRepository.save(user);
        }

        // Ensure engineer profile exists
        if (!engineerProfileRepository.existsByUserId(user.getId())) {
            engineerProfileRepository.save(EngineerProfile.builder()
                    .userId(user.getId())
                    .primarySpecialization(specialization)
                    .activeTaskLimit(taskLimit)
                    .build());
        }

        // Ensure skills are added
        for (String skill : skills) {
            if (!engineerSkillRepository.existsByUserIdAndSkillNameIgnoreCase(user.getId(), skill)) {
                engineerSkillRepository.save(EngineerSkill.builder()
                        .userId(user.getId())
                        .skillName(skill)
                        .build());
            }
        }
    }

    private Role seedRole(String name, String description) {
        return roleRepository.save(Role.builder().name(name).description(description).build());
    }

    private User seedOrUpdateUser(String username, String rawPassword, String email, String fullName, Long roleId) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElseGet(() ->
                User.builder()
                        .username(username)
                        .email(email)
                        .fullName(fullName)
                        .roleId(roleId)
                        .build()
        );
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setRoleId(roleId);
        return userRepository.save(user);
    }
}