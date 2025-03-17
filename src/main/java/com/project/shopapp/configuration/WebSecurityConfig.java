package com.project.shopapp.configuration;

import com.project.shopapp.components.JwtTokenUtil;
import com.project.shopapp.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    @Value("${api.prefix}")
    private String apiPrefix;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request->{
                    request
                            .requestMatchers(
                                    String.format("%s/users/register", apiPrefix),
                                    String.format("%s/users/login", apiPrefix)
                            )
                            .permitAll()
                            .requestMatchers(HttpMethod.PUT,String.format("%s/orders/**",apiPrefix))
                                 .hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET,String.format("%s/orders/**",apiPrefix))
                                    .hasAnyRole("USER","ADMIN")
                            .requestMatchers(HttpMethod.POST,String.format("%s/orders/**",apiPrefix))
                                    .hasAnyRole("USER")
                            .requestMatchers(HttpMethod.DELETE,String.format("%s/orders/**",apiPrefix))
                                    .hasAnyRole("ADMIN")
                            .anyRequest().authenticated();
                })
        ;
        return http.build();
    }
}
