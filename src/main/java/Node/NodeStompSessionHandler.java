package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Component
@Slf4j
public class NodeStompSessionHandler extends StompSessionHandlerAdapter {
    private final CountDownLatch connectionTimeoutLatch;
    private final List<MessageRouteInfo> messageRouteInfos;

    @Autowired
    public NodeStompSessionHandler(
            @Qualifier("Node/ConnectConfig/connectionTimeoutLatch")
            CountDownLatch connectionTimeoutLatch,
            @Qualifier("Node/ConnectConfig/messageRouteInfos")
            List<MessageRouteInfo> messageRouteInfos
    ) {
        this.connectionTimeoutLatch = connectionTimeoutLatch;
        this.messageRouteInfos = messageRouteInfos;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        messageRouteInfos.parallelStream().forEach((messageRouteInfo) -> {
            String subscriptionDestination = messageRouteInfo.getDestinationPath();
            session.subscribe(subscriptionDestination, this);
        });
        //we've connected so cancel the timeout
        connectionTimeoutLatch.countDown();

        log.info("New session: {}", session.getSessionId());
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        log.trace("getting payload type");
        OnceMutableHolder<Type> payloadTypeHolder = new OnceMutableHolder<>(Object.class);
        messageRouteInfos.parallelStream().forEach((messageRouteInfo) -> {
            String routeDestination = messageRouteInfo.getDestinationPath();
            String messageDestination = stompHeaders.getDestination();
            if(routeDestination.equals(messageDestination)) {
                Type payloadType = messageRouteInfo.getMessageClass();
                try {
                    payloadTypeHolder.setThing(payloadType);
                } catch(DuplicateMatchException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage());
                    }
                }
            }
        });
        if(!payloadTypeHolder.isUpdated()) {
            if (log.isErrorEnabled()) {
                log.error("unknown destination to determine payload type {}", stompHeaders.getDestination());
            }
        }
        return payloadTypeHolder.getThing();
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object message) {
        if(log.isDebugEnabled()) {
            log.debug("handling frame. Destination: {}", stompHeaders.getDestination());
        }
        OnceMutableHolder<Consumer> handlerHolder = new OnceMutableHolder<>((failedMessage) -> {
            if (log.isErrorEnabled()) {
                log.error("unable to match destination to determine handler method");
            }
        });
        messageRouteInfos.parallelStream().forEach((messageRouteInfo) -> {
            String routeDestination = messageRouteInfo.getDestinationPath();
            String messageDestination = stompHeaders.getDestination();
            if (routeDestination.equals(messageDestination)) {
                try {
                    handlerHolder.setThing(messageRouteInfo.getHandlerMethod());
                } catch(DuplicateMatchException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage());
                    }
                }
            }
        });
        handlerHolder.getThing().accept(message);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        if(log.isErrorEnabled()) {
            log.error("error handling message: " + exception.getMessage(), exception);
        }
        //we've failed to connect so cancel the timeout
        connectionTimeoutLatch.countDown();
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        if(log.isErrorEnabled()) {
            log.error("error in transport: " + exception.getMessage(), exception);
        }
        //we've failed to connect so cancel the timeout
        connectionTimeoutLatch.countDown();
    }

    private class OnceMutableHolder<T> {
        private T thing;
        private boolean updated;

        public OnceMutableHolder(T defaultThing) {
            thing = defaultThing;
            updated = false;
        }

        public synchronized void setThing(T thing) throws DuplicateMatchException {
            if(updated == true) {
                throw new DuplicateMatchException("trying to update to " + thing.toString() + ", but already updated to " + this.thing.toString());
            }
            this.thing = thing;
            updated = true;
        }

        public T getThing() {
            return thing;
        }

        public boolean isUpdated() {
            return updated;
        }

    }
    public class DuplicateMatchException extends Exception {
        public DuplicateMatchException(String explanation) {
            super(explanation);
        }
    }

}
