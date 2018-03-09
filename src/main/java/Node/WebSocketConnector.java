package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.*;
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
    private final StompSessionHandler sessionHandler;
    private final ThisNodeInfo thisNodeInfo;
    private final CountDownLatch connectionTimeoutLatch;

    @Autowired
    public WebSocketConnector(
        StompSessionHandler sessionHandler,
        @Qualifier("Node/NodeConfigurator/thisNodeInfo")
        ThisNodeInfo thisNodeInfo,
        @Qualifier("Node/NodeConfigurator/connectionTimeoutLatch")
        CountDownLatch connectionTimeoutLatch
    ){
        this.sessionHandler = sessionHandler;
        this.thisNodeInfo = thisNodeInfo;
        this.connectionTimeoutLatch = connectionTimeoutLatch;
    }

    //DO NOT DECLARE AS @BEAN. Must have tight control over when this runs (after delay to let other instances spin up)
    public List<StompSession> getSessions() {
        ConcurrentLinkedQueue<StompSession> sessions = new ConcurrentLinkedQueue<>();
        //lambda to open connection and start sessions
        Consumer<NodeInfo> sessionBuildingLambda = (neighbor -> {
            final WebSocketClient client = new StandardWebSocketClient();
            final List<Transport> transports = new ArrayList<>(2);
            final RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
            final RestTemplate restTemplate = restTemplateBuilder
//                    .setConnectTimeout(30 *1000)
//                    .setReadTimeout(30 *1000)
                    .build();
            final XhrTransport xhrTransport = new RestTemplateXhrTransport(restTemplate);
            transports.add(xhrTransport);
            transports.add(new WebSocketTransport(client));
            final SockJsClient sockJsClient = new SockJsClient(transports);
            final WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            final String uri = UriComponentsBuilder.newInstance()
                    .scheme("ws")
                    .userInfo("node" + String.valueOf(neighbor.getUid()))
                    .host(neighbor.getHostName())
                    .port(neighbor.getPort())
                    .path("ws")
                    .build()
                    .toUriString();
            try {
                log.trace("starting stomp connection");
                final ListenableFuture<StompSession> future = stompClient.connect(uri, sessionHandler);
                //wait for other instances to spin up
                if(!connectionTimeoutLatch.await(30, TimeUnit.SECONDS)) {
                    log.error("failed to connect in 30 seconds");
                }
                final StompSession session = future.get(30, TimeUnit.SECONDS);
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
