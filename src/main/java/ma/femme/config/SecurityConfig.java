package ma.femme.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Los endpoints de RSVP y de la invitación divertida (/fun) quedan públicos
 * (el token en la URL es la propia credencial de acceso a esa invitación). Los endpoints de administración
 * (/admin/**) requieren autenticación HTTP Basic con las credenciales
 * definidas en app.admin.username / app.admin.password.
 *
 * CSRF queda deshabilitado porque esta es una API sin estado (sin sesiones
 * de navegador ni cookies) consumida por clientes REST — la protección CSRF
 * solo tiene sentido para flujos basados en cookies/sesión.
 */
@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Las credenciales no tienen valor por defecto: si no se definen
  // ADMIN_USERNAME y ADMIN_PASSWORD la aplicación no arranca.
  @Bean
  public UserDetailsService userDetailsService(
      PasswordEncoder passwordEncoder,
      @Value("${app.admin.username}") String adminUsername,
      @Value("${app.admin.password}") String adminPassword) {

    return new InMemoryUserDetailsManager(
        User.withUsername(adminUsername)
            .password(passwordEncoder.encode(adminPassword))
            .roles("ADMIN")
            .build());
  }

  // Orígenes del frontend autorizados a llamar a la API desde el navegador
  // (app.cors.allowed-origins, separados por comas; ver application.yml).
  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins}") String[] allowedOrigins) {

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(allowedOrigins));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/rsvp/**", "/fun/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }
}
