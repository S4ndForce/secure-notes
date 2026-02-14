package com.example.security;

import com.example.revoked.RevokedTokenRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final RevokedTokenRepository revokedTokenRepository;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtAuthenticationEntryPoint entryPoint, RevokedTokenRepository revokedTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.entryPoint = entryPoint;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;
        String header = http.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

           try {
               Claims claims = jwtUtil.parseClaims(token);

               String email = claims.getSubject();
               String jti = claims.getId();

               if (revokedTokenRepository.existsByJti(jti)) {
                   log.warn("Attempt to use revoked token: jti={} email={} ip={}",
                           jti, email, http.getRemoteAddr());
                   throw new BadCredentialsException("Token revoked");
               }

               var auth = new UsernamePasswordAuthenticationToken(
                       email,          // principal
                       jti,            // credentials (token identity)
                       Collections.emptyList()

               );
               // Where Authentication object is created
               SecurityContextHolder.getContext().setAuthentication(auth);

           } catch (Exception ex) {
               log.error("JWT authentication failed: error={} path={}",
                       ex.getMessage(), http.getRequestURI());
               SecurityContextHolder.clearContext();
               entryPoint.commence(
                       http,
                       (jakarta.servlet.http.HttpServletResponse) response,
                       new org.springframework.security.authentication.BadCredentialsException(
                               "Invalid JWT", ex
                       ));
               return;
           }
        }

        chain.doFilter(request, response);
    }
}
