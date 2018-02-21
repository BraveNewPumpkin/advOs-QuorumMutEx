package Node;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class MessageBrokerConfig extends AbstractWebSocketMessageBrokerConfigurer {
    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        return new DefaultHandshakeHandler(new TomcatRequestUpgradeStrategy());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //prefix for messages to be forwarded to broker
        config.enableSimpleBroker("/topic");
        //prefix for messages to be forwarded to controllers
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(handshakeHandler())
                .setAllowedOrigins("*")
                .withSockJS();
    }
}

