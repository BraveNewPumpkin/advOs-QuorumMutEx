package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

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

        //log.info("New session: {}", session.getSessionId());
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        Type payloadType = Object.class;
        if(stompHeaders.getSubscription().equals("/topic/leaderElection")) {
            payloadType = LeaderElectionMessage.class;
        }
        return payloadType;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object message) {
        //TODO delegate to controller
        if(stompHeaders.getSubscription().equals("/topic/leaderElection")) {
            try {
                rootService.leaderElection((LeaderElectionMessage)message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        exception.printStackTrace();
        //we've failed to connect so cancel the timeout
        connectionTimeoutLatch.countDown();
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        exception.printStackTrace();
        //we've failed to connect so cancel the timeout
        connectionTimeoutLatch.countDown();
    }
}
