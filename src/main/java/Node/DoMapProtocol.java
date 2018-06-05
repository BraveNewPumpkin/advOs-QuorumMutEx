package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DoMapProtocol implements Runnable{
    private final WebSocketConnector webSocketConnector;
    private final MapController mapController;
    private final MapService mapService;

    @Autowired
    public DoMapProtocol(
            WebSocketConnector webSocketConnector,
            MapController mapController,
            MapService mapService
    ){
        this.webSocketConnector = webSocketConnector;
        this.mapController = mapController;
        this.mapService = mapService;
    }

    @Override
    public void run(){
        log.info("sleeping to allow other instances to spin up");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            log.warn("thread interrupted!");
        }
        log.trace("before getting sessions");
        //get sessions to ensure that they are initialized
        List<StompSession> sessions = webSocketConnector.getSessions();
        log.info("sleeping to allow other instances to SUBSCRIBE");
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            log.warn("thread interrupted!");
        }
        //TODO start the MAP algorithm
    }
}
