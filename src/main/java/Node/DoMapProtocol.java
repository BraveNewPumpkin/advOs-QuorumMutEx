package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DoMapProtocol implements Runnable{
    private final WebSocketConnector webSocketConnector;
    private final MapController mapController;
    private final MapService mapService;
    private Semaphore connectingSynchronizer;
    private final ThisNodeInfo thisNodeInfo;

    @Autowired
    public DoMapProtocol(
            WebSocketConnector webSocketConnector,
            MapController mapController,
            MapService mapService,
            @Qualifier("Node/MapConfig/connectingSynchronizer")
            Semaphore connectingSynchronizer,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo

    ){
        this.webSocketConnector = webSocketConnector;
        this.mapController = mapController;
        this.mapService = mapService;
        this.connectingSynchronizer = connectingSynchronizer;
        this.thisNodeInfo=thisNodeInfo;
    }

    @Override
    public void run(){
        log.info("sleeping to allow other instances to spin up");
        try {
            TimeUnit.SECONDS.sleep(25);
        } catch (InterruptedException e) {
            log.warn("thread interrupted!");
        }
        log.trace("before getting sessions");
        //get sessions to ensure that they are initialized
        List<StompSession> sessions = webSocketConnector.getSessions();
        log.info("sleeping to allow other instances to SUBSCRIBE");
        try {
            TimeUnit.SECONDS.sleep(25);
        } catch (InterruptedException e) {
            log.warn("thread interrupted!");
        }
        connectingSynchronizer.release();
        log.trace("waiting for tree to be built to start MAP protocol");

        if(thisNodeInfo.getUid()==0) {
            mapService.doActiveThings();
        }
    }

}
