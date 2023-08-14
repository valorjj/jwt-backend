package jj.study.auctionbackend.config.security;


import jj.study.auctionbackend.common.env.CustomJwtProperties;
import jj.study.auctionbackend.common.env.CustomOAuth2Properties;
import jj.study.auctionbackend.config.security.exception.CustomAuthenticationEntryPoint;
import jj.study.auctionbackend.config.security.filter.TokenAuthenticationFilter;
import jj.study.auctionbackend.config.security.handler.OAuth2AuthenticationFailureHandler;
import jj.study.auctionbackend.config.security.handler.OAuth2AuthenticationSuccessHandler;
import jj.study.auctionbackend.config.security.handler.CustomAccessDeniedHandler;
import jj.study.auctionbackend.config.security.mapper.CustomAuthoritiesMapper;
import jj.study.auctionbackend.config.security.provider.AuthTokenProvider;
import jj.study.auctionbackend.repository.security.OAuth2AuthorizationRequestBasedOnCookieRepository;
import jj.study.auctionbackend.repository.security.UserRefreshTokenRepository;
import jj.study.auctionbackend.service.user.CustomOAuth2UserService;
import jj.study.auctionbackend.service.user.CustomOidcUserService;
import jj.study.auctionbackend.service.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;


import static org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfig corsConfig;
    private final AuthTokenProvider authTokenProvider;
    private final CustomJwtProperties customJwtProperties;
    private final CustomOAuth2Properties customOAuth2Properties;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final CustomAuthoritiesMapper customAuthoritiesMapper;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UserRefreshTokenRepository userRefreshTokenRepository;

    @Scope("prototype")
    @Bean
    public Builder mvc(HandlerMappingIntrospector introspector) {
        return new Builder(introspector);
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, Builder mvcPattern) throws Exception {

        // (1) CSRF / disabled
        http.csrf(AbstractHttpConfigurer::disable);

        // (2) CORS
        http.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()));

        // (3)
        http.authorizeHttpRequests(
                requests -> requests
                        .requestMatchers(
                                "/"
                        ).permitAll()
                        .anyRequest().authenticated()
        );

        // (4) Session / stateless
        http.sessionManagement(sessionManagementConfigurer ->
                sessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // (5) Configure OAuth 2.0 login
        http.oauth2Login(
                login -> login
                        // (5-1) Configure userinfo endpoint provided by the authorization server
                        .userInfoEndpoint(
                                userInfoEndpointConfig ->
                                        userInfoEndpointConfig
                                                .userService(customOAuth2UserService)
                                                .oidcUserService(customOidcUserService)
                                                .userAuthoritiesMapper(customAuthoritiesMapper)
                        )
                        // (5-2) Authorization Endpoint is customizable, currently set to default
                        .authorizationEndpoint(
                                authorizationEndpointConfig ->
                                        authorizationEndpointConfig
                                                // (5-2-1)
                                                .baseUri("/oauth2/authorization")
                                                // (5-2-2) Cookie based authorization respository
                                                .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                        )
                        // (5-3) Redirection Endpoint is customizable, currently set to default
                        .redirectionEndpoint(
                                redirectionEndpointConfig ->
                                        redirectionEndpointConfig
                                                // (5-3-1)
                                                .baseUri("/*/oauth2/code/*")
                        )
                        // (5-4)
                        .loginPage("/")
                        // (5-5) Register success handler
                        .successHandler(oAuth2AuthenticationSuccessHandler())
                        // (5-6) Register failure handler
                        .failureHandler(oAuth2AuthenticationFailureHandler())
        );

        // (6) UserDetailsService
        http.userDetailsService(customUserDetailsService);

        // (7) Logout
        http.logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                .logoutSuccessUrl("/").permitAll()
                // .deleteCookies("JSESSIONID")
                // .invalidateHttpSession(true)
                .clearAuthentication(true)
        );

        // (8) Exception Handling
        http.exceptionHandling(httpSecurityExceptionHandler -> httpSecurityExceptionHandler
                // (8-1)
                .accessDeniedHandler(customAccessDeniedHandler)
                // (8-2)
                .authenticationEntryPoint(customAuthenticationEntryPoint)
        );

        // (9) Form Login
        // Can`t find a way to disable this
        http.formLogin(Customizer.withDefaults());

        // (10) Add Filter
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // (11) Http Basic / disabled
        http.httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * Cookie 기반 Authorization Repository
     */
    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository(customJwtProperties);
    }

    /**
     * @return
     */
    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(authTokenProvider);
    }

    /**
     * @return
     */
    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(authTokenProvider,
                userRefreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                customJwtProperties,
                customOAuth2Properties
        );
    }

    /**
     * @return
     */
    @Bean
    public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler(oAuth2AuthorizationRequestBasedOnCookieRepository(),
                customJwtProperties
        );
    }

}
