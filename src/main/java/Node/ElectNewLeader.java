package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

@Component
@Slf4j
public class ElectNewLeader implements Runnable{
    private final WebSocketConnector webSocketConnector;
    private final LeaderElectionController leaderElectionController;
    private ReadWriteLock sendingInitialLeaderElectionMessage;

    @Autowired
    public ElectNewLeader(
        WebSocketConnector webSocketConnector,
        LeaderElectionController leaderElectionController,
        @Qualifier("Node/LeaderElectionConfig/sendingInitialLeaderElectionMessage")
        ReadWriteLock sendingInitialLeaderElectionMessage
    ){
        this.webSocketConnector = webSocketConnector;
        this.sendingInitialLeaderElectionMessage = sendingInitialLeaderElectionMessage;
        this.leaderElectionController = leaderElectionController;
    }

    @Override
    public void run(){
        sendingInitialLeaderElectionMessage.writeLock().lock();
        try {
            log.info("sleeping to allow other instances to spin up");
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                log.warn("leader election thread interrupted!");
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
            log.trace("before sending leader election message");
            leaderElectionController.sendLeaderElection();
        } finally {
            sendingInitialLeaderElectionMessage.writeLock().unlock();
        }
    }
}
