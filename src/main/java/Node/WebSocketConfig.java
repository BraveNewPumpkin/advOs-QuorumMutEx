package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.List;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private List<String> subscriptionsDestinations;

    @Autowired
    public WebSocketConfig(
        @Qualifier("Node/ConnectConfig/subscriptionDestinations")
        List<String> subscriptionsDestinations

    ){
        super();
        this.subscriptionsDestinations = subscriptionsDestinations;
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new TextWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] subscriptions = subscriptionsDestinations.toArray(new String[subscriptionsDestinations.size()]);
        registry.addHandler(webSocketHandler(), subscriptions)
                .setHandshakeHandler(new DefaultHandshakeHandler(new TomcatRequestUpgradeStrategy()))
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
