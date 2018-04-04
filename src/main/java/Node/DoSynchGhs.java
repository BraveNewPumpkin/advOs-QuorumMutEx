package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DoSynchGhs implements Runnable{
    private final WebSocketConnector webSocketConnector;
    private final SynchGhsController synchGhsController;
    private final SynchGhsService synchGhsService;
    private final GateLock sendingInitialMwoeSearchMessage;


    @Autowired
    public DoSynchGhs(
            WebSocketConnector webSocketConnector,
            SynchGhsController synchGhsController,
            SynchGhsService synchGhsService,
            @Qualifier("Node/SynchGhsConfig/sendingInitialMwoeSearchMessage")
            GateLock sendingInitialMwoeSearchMessage
    ){
        this.webSocketConnector = webSocketConnector;
        this.sendingInitialMwoeSearchMessage = sendingInitialMwoeSearchMessage;
        this.synchGhsController = synchGhsController;
        this.synchGhsService = synchGhsService;
    }

    @Override
    public void run(){
        sendingInitialMwoeSearchMessage.close();
        log.info("sleeping to allow other instances to spin up");
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            log.warn("SynchGhs thread interrupted!");
        }
        log.trace("before getting sessions");
        //get sessions to ensure that they are initialized
        List<StompSession> sessions = webSocketConnector.getSessions();
        log.info("sleeping to allow other instances to SUBSCRIBE");
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            log.warn("thread interrupted!");
        }
        synchGhsService.markAsSearched();
        log.trace("before sending MwoeSearch message");
        synchGhsController.sendMwoeSearch();
        sendingInitialMwoeSearchMessage.open();
    }
}
