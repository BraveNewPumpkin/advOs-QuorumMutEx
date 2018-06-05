package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Semaphore;

@Component
@Slf4j
public class DoMapProtocol implements Runnable{
    private final WebSocketConnector webSocketConnector;
    private final MapController mapController;
    private final MapService mapService;
    private Semaphore connectingSynchronizer;

    @Autowired
    public DoMapProtocol(
            WebSocketConnector webSocketConnector,
            MapController mapController,
            MapService mapService,
            @Qualifier("Node/MapConfig/connectingSynchronizer")
            Semaphore connectingSynchronizer
    ){
        this.webSocketConnector = webSocketConnector;
        this.mapController = mapController;
        this.mapService = mapService;
        this.connectingSynchronizer = connectingSynchronizer;
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
        connectingSynchronizer.release();
        //TODO start the MAP algorithm
        NodeInfo chosenNeighbor = mapService.chooseRandomNeighbor();
        mapController.sendMapMessage(chosenNeighbor.getUid());
    }

}
