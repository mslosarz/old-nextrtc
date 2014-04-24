package org.nextrtc.signaling;

import org.nextrc.signaling.SignalingEndpoint;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = SignalingEndpoint.class)
class Config {
}