package org.nextrtc.server;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = NextRTCEndpoint.class)
public class Config {
}