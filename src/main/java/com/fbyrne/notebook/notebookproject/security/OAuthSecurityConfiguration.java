package com.fbyrne.notebook.notebookproject.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("!test")
public class OAuthSecurityConfiguration {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // Disable default security.
        http.httpBasic().disable();
        http.formLogin().disable();
        http.csrf().disable();
        http.logout().disable();

        http.requestCache().requestCache(NoOpServerRequestCache.getInstance());
        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        http.oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer
                        .jwt(withDefaults())
        );

        http.authorizeExchange().anyExchange().authenticated();

        return http.build();
    }

}
