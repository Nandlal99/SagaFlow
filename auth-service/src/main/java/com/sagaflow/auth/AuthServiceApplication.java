package com.sagaflow.auth;

import com.sagaflow.security.BaseSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(BaseSecurityConfig.class)
public class AuthServiceApplication {

    public static void main(String[] args){
        SpringApplication.run(AuthServiceApplication.class);
    }
}
