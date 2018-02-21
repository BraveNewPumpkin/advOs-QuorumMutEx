package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class NodeStompSessionHandler extends StompSessionHandlerAdapter {
    @Autowired
    private RootService rootService;

    private CountDownLatch connectionTimeoutLatch;

    public NodeStompSessionHandler(CountDownLatch connectionTimeoutLatch) {
        this.connectionTimeoutLatch = connectionTimeoutLatch;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        session.subscribe("/topic/leaderElection", this);
        //we've connected so cancel the timeout
        connectionTimeoutLatch.countDown();

        log.info("New session: {}", session.getSessionId());
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        //TODO: convert to trace
        log.error("--------getting payload type");
        Type payloadType = Object.class;
        if(stompHeaders.getDestination().equals("/topic/leaderElection")) {
            payloadType = LeaderElectionMessage.class;
        }
        return payloadType;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object message) {
        //TODO: convert to trace
        log.error("--------handling frame. destination: " + stompHeaders.getDestination());// + " message: " + ((LinkedHashMap<String, String>)message).toString());
        //TODO delegate to controller
        if(stompHeaders.getDestination().equals("/topic/leaderElection")) {
            try {
                rootService.leaderElection((LeaderElectionMessage)message);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.error(exception.getMessage());
        //we've failed to connect so cancel the timeout
        connectionTimeoutLatch.countDown();
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        log.error(exception.getMessage());
        //we've failed to connect so cancel the timeout
        connectionTimeoutLatch.countDown();
    }
}
