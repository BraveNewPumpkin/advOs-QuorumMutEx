package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;

public class NodeStompSessionHandler extends StompSessionHandlerAdapter {
    @Autowired
    private RootService rootService;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        session.subscribe("/topic/leaderElection", this);

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
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        exception.printStackTrace();
    }
}
