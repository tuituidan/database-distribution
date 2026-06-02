package com.tuituidan.openhub.config;

import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

/**
 * SecurityConfig.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/3/3
 */
@Slf4j
@Configuration
@EnableWebSecurity
@ConfigurationProperties(prefix = "spring.security")
public class SecurityConfig {

    @Getter
    @Setter
    private Boolean enabled;

    /**
     * filterChain
     *
     * @param http http
     * @return SecurityFilterChain
     * @throws Exception Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();
        http.csrf().disable();
        if (BooleanUtils.isTrue(enabled)) {
            setLogin(http.formLogin());
            setLogout(http.logout());
            http.authorizeHttpRequests().antMatchers("/api/v1/**")
                    .authenticated().anyRequest().permitAll();
            http.exceptionHandling().defaultAuthenticationEntryPointFor((request, response, ex) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED),
                    request -> "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With")));
        } else {
            http.authorizeRequests().anyRequest().permitAll();
        }
        return http.build();
    }

    private void setLogin(FormLoginConfigurer<HttpSecurity> login) {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("returnUrl");
        successHandler.setUseReferer(true);

        login.loginPage("/login").loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureHandler(new CustomAuthenticationFailureHandler())
                .permitAll();
    }

    private void setLogout(LogoutConfigurer<HttpSecurity> logout) {
        SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
        handler.setUseReferer(true);
        logout.logoutUrl("/logout").logoutSuccessHandler(handler).deleteCookies("JSESSIONID");
    }

}

