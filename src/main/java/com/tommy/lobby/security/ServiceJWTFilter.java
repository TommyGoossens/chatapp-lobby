package com.tommy.lobby.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.tommy.lobby.security.SecurityConstants.HEADER_STRING;
import static com.tommy.lobby.security.SecurityConstants.TOKEN_PREFIX;

@Component
public class ServiceJWTFilter extends OncePerRequestFilter {

    @Autowired
    private ServiceJWTReader jWTReader;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = getTokenFromHeader(request);

        if(token != null){
            Authentication authentication = jWTReader.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request,response);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        if (token == null || token.isEmpty()) { return null; }

        return token.replace(TOKEN_PREFIX,"");
    }
}
