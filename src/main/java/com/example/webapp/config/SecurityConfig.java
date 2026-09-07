package com.example.webapp.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // 認可設定
            .authorizeHttpRequests(auth -> auth
                    // 404などのエラー画面への転送を許可する
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    // 静的リソースは誰でもアクセス可能
                    .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                    // 誰でもOK画面
                    .requestMatchers("/", "/users/login","/users/signup","/error").permitAll()
                    // それ以外はログイン必須
                    .anyRequest().authenticated())

            // ログイン設定
            .formLogin(form -> form
                    .loginPage("/users/login")       // 自作ログインページ(GET)
                    .loginProcessingUrl("/users/login") // フォームのPOST先(Spring Securityが処理)
                    .usernameParameter("userid")   // 学籍番号を送るフィールド名
                    .passwordParameter("password")
                    .defaultSuccessUrl("/", true)    // 成功したらトップ画面へ
                    .permitAll())

            // ログアウト設定
            .logout(logout -> logout
                    .logoutUrl("/users/logout")
                    .logoutSuccessUrl("/")           // ログアウト後もトップ画面へ
                    .permitAll())

            // HTTP Basic 認証は無効化
            .httpBasic(AbstractHttpConfigurer::disable)
            // CSRF 保護は有効(デフォルト)
            .csrf(Customizer.withDefaults());

        return http.build();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 学習用のため平文のまま比較
        return NoOpPasswordEncoder.getInstance();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}