package com.diversestudio.unityapi.config;

import com.diversestudio.unityapi.entities.Role;
import com.diversestudio.unityapi.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupInitializer {

    @Bean
    public CommandLineRunner initializeRoles(RoleRepository roleRepository)
    {
        return args -> {

            if (roleRepository.findByRoleName(("ADMIN")).isEmpty())
            {
                Role admin = new Role();
                admin.setRoleName("ADMIN");
                roleRepository.save(admin);
            }

            if (roleRepository.findByRoleName(("USER")).isEmpty())
            {
                Role user = new Role();
                user.setRoleName("USER");
                roleRepository.save(user);
            }
        };
    }
}
