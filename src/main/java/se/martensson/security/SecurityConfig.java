package se.martensson.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	public static final String LOGOUT = "/logout";
	public static final String LOGIN = "/login";
	
	@Override
	  protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
			.authorizeRequests()
				.anyRequest()
				.authenticated()
			.and()
			.formLogin()
				.successForwardUrl("/")
			.and()
		    .logout()
		        .logoutRequestMatcher(new AntPathRequestMatcher(LOGOUT))            
		        .logoutSuccessUrl(LOGIN)
		        .invalidateHttpSession(true)      
		        .deleteCookies("JSESSIONID")        
		        .and()
		    .exceptionHandling()
		        .accessDeniedPage("/403");
	  }
	
	@Override
	  public void configure(WebSecurity web) {
	    	web.ignoring().antMatchers("/VAADIN/**");
	  }
}