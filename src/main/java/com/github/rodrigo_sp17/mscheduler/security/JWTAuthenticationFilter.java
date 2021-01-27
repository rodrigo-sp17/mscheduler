package com.github.rodrigo_sp17.mscheduler.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    protected static final String SECRET = "6SYhdnZ6YCq1ChjLda0hv1zA9GiaUx6alkZtqbVp12+pbMZbYaD0F" +
            "xiSHoaCzJ4hn6Zz3TgYn38rDOQDax5e1Q==";

    @Autowired
    private final AuthenticationManager manager;

    private final ObjectMapper mapper;

    public JWTAuthenticationFilter(AuthenticationManager manager) {
        this.manager = manager;
        this.mapper = new ObjectMapper();
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {
        try {
            CreateUserRequest user = mapper.readValue(request.getReader(), CreateUserRequest.class);
            var token = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), user.getPassword());

            setDetails(request, token);
            return manager.authenticate(token);
        } catch (IOException e) {
            log.error("Could not read user from request");
            throw new AuthenticationServiceException(e.getMessage());
        } catch (AuthenticationException a) {
            log.info("Failed authentication attempt");
            throw a;
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        User user = (User) authResult.getPrincipal();
        String jwtToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(Date.from(LocalDate.now().plusDays(10)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()))
                .sign(Algorithm.HMAC512(SECRET));

        response.addHeader("Authorization", "Bearer " + jwtToken);
    }
}
