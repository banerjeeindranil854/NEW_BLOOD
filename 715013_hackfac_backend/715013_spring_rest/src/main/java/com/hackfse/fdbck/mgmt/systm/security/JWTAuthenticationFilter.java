package com.hackfse.fdbck.mgmt.systm.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hackfse.fdbck.mgmt.systm.cnst.AppConstants;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

public class JWTAuthenticationFilter extends OncePerRequestFilter {
	
	private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(AppConstants.AUTH_HDR);
		
		log.info(request.getRequestURI());
		log.info(request.getMethod());
		log.info(request.getMethod());
		log.info(request.getHeader(AppConstants.AUTH_HDR));
		String username = null;
		String authToken = null;
		if (header != null && header.contains(AppConstants.BEARER_ENTRY)) {
			authToken = header.replace(AppConstants.BEARER_ENTRY, StringUtils.EMPTY);
			try {
				username = jwtTokenProvider.getUsernameFromJWT(authToken);
			} catch (IllegalArgumentException e) {
				log.error("An error occured during getting username from token", e);
			} catch (ExpiredJwtException e) {
				log.warn("The token is expired and not valid anymore", e);
			} catch (SignatureException e) {
				log.error("Authentication Failed. Username or Password not valid.");
			}
		} else {
			log.warn("couldn't find bearer string, will ignore the header");
		}
		if (username != null && jwtTokenProvider.validateToken(authToken)) {
			UsernamePasswordAuthenticationToken authentication = jwtTokenProvider.getAuthentication(authToken);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			log.info("Setting security context. Authenticated user name is {}", username );
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		filterChain.doFilter(request, response);
	}
}
