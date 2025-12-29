package com.aa.msw.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Profile("!test")
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        http.oauth2ResourceServer(oAuth2ResourceServerConfigurer -> oAuth2ResourceServerConfigurer
                .jwt(withDefaults())
                .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(converter)));
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(getMatchersForRequestsNotToBeAuthenticated()).permitAll()
                .anyRequest().authenticated()
        ).httpBasic(withDefaults());
        return http.build();
    }

    RequestMatcher[] getMatchersForRequestsNotToBeAuthenticated() {
        return new AntPathRequestMatcher[]{
                new AntPathRequestMatcher("/api/v1/spots/public", HttpMethod.GET.toString()),
                new AntPathRequestMatcher("/api/v1/stations", HttpMethod.GET.toString()),
                new AntPathRequestMatcher("/api/v1/historicalYears", HttpMethod.GET.toString()),
                new AntPathRequestMatcher("/api/v1/forecasts", HttpMethod.GET.toString()),
                new AntPathRequestMatcher("/api/v1/spots", HttpMethod.GET.toString()),
                new AntPathRequestMatcher("/api/v1/sample/last40Days/*", HttpMethod.GET.toString()),
        };
    }
}
