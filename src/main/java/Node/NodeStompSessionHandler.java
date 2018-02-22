package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class NodeStompSessionHandler extends StompSessionHandlerAdapter {
    private LeaderElectionController leaderElectionController;
    private CountDownLatch connectionTimeoutLatch;

    public NodeStompSessionHandler(ApplicationContext context, CountDownLatch connectionTimeoutLatch) {
        this.connectionTimeoutLatch = connectionTimeoutLatch;
        this.leaderElectionController = context.getBean(LeaderElectionController.class);
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        session.subscribe("/topic/leaderElection", this);
//        session.subscribe("/topic/leaderElection", webSocketHandler);
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
        if(stompHeaders.getDestination().equals("/topic/leaderElection")) {
            //TODO: convert to trace
            log.error("--------calling RootService.leaderElection");
            LeaderElectionMessage leaderElectionMessage = (LeaderElectionMessage)message;
            leaderElectionController.leaderElection(leaderElectionMessage);
        }
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.error("error handling message: " + exception.getMessage());
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
