package com.example.ddakdaegi.global.config.security;


import com.example.ddakdaegi.global.filter.JwtExceptionFilter;
import com.example.ddakdaegi.global.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;


@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtFilter jwtFilter;
	private final JwtExceptionFilter jwtExceptionFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		//jwt 와 같이 사용중이므로 spring security에서는 인가와 관련된 작업만 설정

		http.csrf(AbstractHttpConfigurer::disable) //폼 기반 로그인 방식이 아니라면 보통 CSRF를 비활성화함
			.cors(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)//로그인은 따로 구현된 api로 처리
			.anonymous(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.rememberMe(AbstractHttpConfigurer::disable)
			.addFilterBefore(jwtExceptionFilter, SecurityContextHolderAwareRequestFilter.class)
			.addFilterBefore(jwtFilter, SecurityContextHolderAwareRequestFilter.class)
			//인가 설정
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
				.requestMatchers("/api/v1/auth/**", "/health", "versions").permitAll()
				.anyRequest().authenticated() // 사용자 관련 api 구현시 주석 해제
			)
			//세션은 사용하지않도록 설정
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
