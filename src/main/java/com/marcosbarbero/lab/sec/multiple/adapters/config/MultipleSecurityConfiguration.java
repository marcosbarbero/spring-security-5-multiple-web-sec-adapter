package com.marcosbarbero.lab.sec.multiple.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static com.marcosbarbero.lab.sec.multiple.adapters.config.MultipleSecurityConfiguration.ApiSecurityConfiguration.ORDER;

@EnableWebSecurity
public class MultipleSecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Configuration
    @Order(ORDER)
    static class ApiSecurityConfiguration extends WebSecurityConfigurerAdapter {

        static final int ORDER = 1;

        private final PasswordEncoder passwordEncoder;

        ApiSecurityConfiguration(final PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .antMatchers("/api/**")
                    .authenticated();
//            .and()
////                    .addFilterBefore(new OAuth2ClientAuthenticationProcessingFilter(), BasicAuthenticationFilter.class)
//                    .authorizeRequests()
//                    .antMatchers("/api/**").authenticated();
        }

        @Override
        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService())
                    .passwordEncoder(passwordEncoder);
        }

        @Override
        @Bean("apiUserDetailsService")
        protected UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.builder()
                            .passwordEncoder(passwordEncoder::encode)
                            .username("apiuser")
                            .password("pass")
                            .roles("API")
                            .build()
            );
        }

        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

    }

    @Configuration
    @Order(ORDER + 1)
    static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

        private final PasswordEncoder passwordEncoder;

        WebSecurityConfiguration(final PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http
                    .formLogin()
                    .permitAll()
                    .successForwardUrl("/welcome")
                    .and()
                    .authorizeRequests().antMatchers("/**").authenticated();
        }

        @Override
        public void configure(final WebSecurity web) {
            web.ignoring().antMatchers("/non-secure/**");
        }

        @Override
        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService())
                    .passwordEncoder(passwordEncoder);
        }

        @Override
        @Bean("formUserDetailsService")
        protected UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.builder()
                            .passwordEncoder(passwordEncoder::encode)
                            .username("user")
                            .password("pass")
                            .roles("USER")
                            .build()
            );
        }
    }

}
