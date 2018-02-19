package Node;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static Node.LambdaExceptionUtil.rethrowConsumer;

@Configuration
public class WebSocketConnector {

    @Autowired
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    ThisNodeInfo thisNodeInfo;


    @Bean
    @Qualifier("Node/WebSocketConnector/sessions")
    public List<StompSession> getSessions() throws InterruptedException, TimeoutException, ExecutionException {
        ConcurrentLinkedQueue<StompSession> sessions = new ConcurrentLinkedQueue<>();
        //lambda to open connection and start sessions
        Consumer<NodeInfo> sessionBuildingLambda = rethrowConsumer(neighbor -> {
            final CountDownLatch connectionTimeoutLatch = new CountDownLatch(1);
            StompSessionHandler sessionHandler = new NodeStompSessionHandler(connectionTimeoutLatch);
            WebSocketClient client = new StandardWebSocketClient();
            List<Transport> transports = new ArrayList<>(1);
            transports.add(new WebSocketTransport(client));
            SockJsClient sockJsClient = new SockJsClient(transports);
            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            //TODO look up how to specify port
            String uri = UriComponentsBuilder.newInstance()
                    .scheme("ws")
                    .userInfo(String.valueOf(neighbor.getUid()))
                    .host(neighbor.getHostName())
                    .port(neighbor.getPort())
                    .build()
                    .toUriString();
            ListenableFuture<StompSession> future = stompClient.connect(uri, sessionHandler);
            //wait for other instances to spin up
            if(!connectionTimeoutLatch.await(30000, TimeUnit.SECONDS)) {
                throw new TimeoutException("failed ot connect in 30 seconds");
            }
            StompSession session = future.get();
            sessions.add(session);
        });
        //run the lambda in parallel for each neighboring node
        thisNodeInfo.getNeighbors().parallelStream().forEach(sessionBuildingLambda);

        return Collections.unmodifiableList(Arrays.asList(sessions.toArray(new StompSession[0])));
    }

}
