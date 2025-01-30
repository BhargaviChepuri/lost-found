package com.mss.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// Allow all domains, methods, and headers
		registry.addMapping("/**") // Apply to all paths
				.allowedOriginPatterns("*") // Allow any origin dynamically, with pattern matching
				.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // Allowed HTTP methods
				.allowedHeaders("*") // Allow all headers
				.allowCredentials(true); // Allow credentials (cookies, authorization headers)
	}
}
