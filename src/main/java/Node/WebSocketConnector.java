package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class WebSocketConnector {
    //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebSocketConnector.class);
    //private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    private ThisNodeInfo thisNodeInfo;

    @Bean
    @Qualifier("Node/WebSocketConnector/sessions")
    public List<StompSession> getSessions() {
        ConcurrentLinkedQueue<StompSession> sessions = new ConcurrentLinkedQueue<>();
        //lambda to open connection and start sessions
        Consumer<NodeInfo> sessionBuildingLambda = (neighbor -> {
            final CountDownLatch connectionTimeoutLatch = new CountDownLatch(1);
            final StompSessionHandler sessionHandler = new NodeStompSessionHandler(connectionTimeoutLatch);
            final List<Transport> transports = new ArrayList<>(1);
            final WebSocketClient client = new StandardWebSocketClient();
            transports.add(new WebSocketTransport(client));
            final SockJsClient sockJsClient = new SockJsClient(transports);
//            final WebSocketClient sockJsClient = new SockJsClient(transports);
            final WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            final String uri = UriComponentsBuilder.newInstance()
                    .scheme("ws")
                    .userInfo("node" + String.valueOf(neighbor.getUid()))
                    .host(neighbor.getHostName())
                    .port(neighbor.getPort())
                    .build()
                    .toUriString();
            try {
                final ListenableFuture<StompSession> future = stompClient.connect(uri, sessionHandler);
                //wait for other instances to spin up
                if(!connectionTimeoutLatch.await(5, TimeUnit.SECONDS)) {
                    log.error("failed ot connect in 5 seconds");
                }
                final StompSession session = future.get(5, TimeUnit.SECONDS);
                sessions.add(session);
            }catch(Throwable t) {
                log.error(t.getMessage());
            }
        });
        //run the lambda in parallel for each neighboring node
        thisNodeInfo.getNeighbors().parallelStream().forEach(sessionBuildingLambda);

        return Collections.unmodifiableList(Arrays.asList(sessions.toArray(new StompSession[0])));
    }

}
