package com.example.demo.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {
	
	private final String IGNORE = "C:/workspace-spring-tool-suite-4-4.6.0.RELEASE/cursospringboot34/src/main/resources/static";
	private final String senhaCriptografada = "$2a$10$DS23E4VycGysX.sPz7eiyentwDGJh5uKGiByAzwmw5uRESrfat2Ge";
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable() 
			.authorizeRequests() 
			.antMatchers(HttpMethod.GET, "/").permitAll() 
			.anyRequest().authenticated() 
			.and().formLogin().permitAll() 
			.and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
			.withUser("vinicius")
			.password(senhaCriptografada)
			.roles("ADMIN");
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(IGNORE);
	}
	
	
}
