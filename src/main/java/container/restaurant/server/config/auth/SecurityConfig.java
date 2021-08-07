package container.restaurant.server.config.auth;

import container.restaurant.server.utils.jwt.JwtLoginService;
import container.restaurant.server.utils.jwt.jjwt.JjwtLoginService;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .cors().and().csrf().disable()
//                .sessionManagement(s -> s
//                        .sessionCreationPolicy(SessionCreationPolicy.NEVER))
//                .exceptionHandling(e -> e
//                        .authenticationEntryPoint((request, response, ex) ->
//                                response.sendError(HttpStatus.SC_UNAUTHORIZED, ex.getMessage())))
//                .authorizeRequests(a -> a
//                        .antMatchers(GET).permitAll()
//                        .antMatchers(POST, "/api/user/login", "/api/user").permitAll()
//                        .anyRequest().authenticated());


        http
                .csrf().disable()
                .headers(h -> h
                        .frameOptions().disable()
                )
                .authorizeRequests(a -> a
                        .anyRequest().permitAll()
                )
                .logout(l -> l
                        .logoutSuccessUrl("/")
                )
                .oauth2Login(o -> o
                        .userInfoEndpoint().userService(customOAuth2UserService)
                );
    }
}
