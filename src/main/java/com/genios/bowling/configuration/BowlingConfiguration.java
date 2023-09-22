package com.genios.bowling.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bowling")
public class BowlingConfiguration {

    @Getter
    @Setter
    private int lines;
}
