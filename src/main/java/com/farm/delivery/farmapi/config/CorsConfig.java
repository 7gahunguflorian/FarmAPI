package com.farm.delivery.farmapi.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // Only allow your frontend origin
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        config.setAllowCredentials(true);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*")); // Allow all headers
        config.setExposedHeaders(Arrays.asList("Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}


// package com.farm.delivery.farmapi.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;

// import java.util.Arrays;
// import java.util.List;

// @Configuration
// public class CorsConfig {

//     @Bean
//     public CorsFilter corsFilter() {
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         CorsConfiguration config = new CorsConfiguration();

//         // Allow only the frontend origin
//         config.setAllowedOrigins(Arrays.asList(
//             "http://localhost:3000",
//             "https://extensions.aitopia.ai"
//         ));
        
//         // Allow all HTTP methods
//         config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
//         // Allow all necessary headers
//         config.setAllowedHeaders(Arrays.asList(
//             "Authorization",
//             "Content-Type",
//             "X-Requested-With",
//             "Accept",
//             "Origin",
//             "Access-Control-Request-Method",
//             "Access-Control-Request-Headers"
//         ));
        
//         // Expose headers
//         config.setExposedHeaders(Arrays.asList(
//             "Authorization",
//             "Access-Control-Allow-Origin",
//             "Access-Control-Allow-Credentials"
//         ));
        
//         // Allow credentials
//         config.setAllowCredentials(true);
        
//         // Cache preflight requests for 1 hour
//         config.setMaxAge(3600L);

//         source.registerCorsConfiguration("/**", config);
//         return new CorsFilter(source);
//     }
// }