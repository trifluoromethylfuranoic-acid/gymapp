package com.epam.lenda.gymapp.common;

import com.epam.lenda.gymapp.common.logging.LoggingConfig;
import com.epam.lenda.gymapp.common.security.ResourceServerConfig;
import com.epam.lenda.gymapp.common.security.ServiceTokenConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({LoggingConfig.class, ResourceServerConfig.class, ServiceTokenConfig.class})
public class Config {
}
