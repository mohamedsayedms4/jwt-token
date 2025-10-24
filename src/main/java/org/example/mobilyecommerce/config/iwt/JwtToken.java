package org.example.mobilyecommerce.config.iwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@ConfigurationProperties(prefix = "token")
@Getter
@Setter
@Configuration
public class JwtToken {
    private String secret ;
    private Duration accessTime;
    private Duration refreshTime;
}
