package com.virtusa.traderapi.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.virtusa.traderapi.configurations.JwtUtil;
import com.virtusa.traderapi.exceptions.JwtTokenMissingException;
import com.virtusa.traderapi.models.User;
import com.virtusa.traderapi.services.UserAuthService;




@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserAuthService userAuthService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String header = request.getHeader("Authorization");

		if (header == null || !header.startsWith("Bearer")) {
			throw new JwtTokenMissingException("No JWT token found in the request headers");
		}

		String token = header.substring(7);

		// Optional - verification
		jwtUtil.validateToken(token);

		User user = jwtUtil.getUser(token);

		UserDetails userDetails = userAuthService.loadUserByUsername(user.getUserName());

		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());

		usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		}

		filterChain.doFilter(request, response);
	}

}
