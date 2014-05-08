package org.nextrtc.server;

import org.nextrtc.server.NextRTCEndpoint;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = NextRTCEndpoint.class)
class Config {
}