package com.codewithme.compiler.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtAuthenticationFilter implements Filter {

    private static final String SECRET_KEY="salahapplication";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authorizationHeader = httpRequest.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {

                Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser()
                        .setSigningKey(key)
                        .parseClaimsJws(token)
                        .getBody();

                String email = claims.getSubject();
                System.out.println("Email extracted from JWT: " + email);


                httpRequest.setAttribute("email", email);

            } catch (Exception e) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                return;
            }
        } else {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header missing or invalid");
            return;
        }

        chain.doFilter(request, response);
    }
}
