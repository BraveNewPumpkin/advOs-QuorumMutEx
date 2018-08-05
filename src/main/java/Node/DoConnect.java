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
public class DoConnect implements Runnable {
    private final WebSocketConnector webSocketConnector;
    private final Semaphore connectingSynchronizer;
    private final ThisNodeInfo thisNodeInfo;

    @Autowired
    public DoConnect (
            WebSocketConnector webSocketConnector,
            @Qualifier("Node/ConnectConfig/connectingSynchronizer")
            Semaphore connectingSynchronizer,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo

    ){
        this.webSocketConnector = webSocketConnector;
        this.connectingSynchronizer = connectingSynchronizer;
        this.thisNodeInfo=thisNodeInfo;
    }

    @Override
    public void run(){
        log.info("sleeping to allow other instances to spin up");
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.warn("thread interrupted!");
        }
        log.trace("before getting sessions");
        //get sessions to ensure that they are initialized
        List<StompSession> sessions = webSocketConnector.getSessions();
        log.info("sleeping to allow other instances to SUBSCRIBE");
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.warn("thread interrupted!");
        }
        connectingSynchronizer.release();
    }
}
