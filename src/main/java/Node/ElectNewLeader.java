package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.List;

@Slf4j
public class ElectNewLeader implements Runnable{
    private ApplicationContext context;

    public ElectNewLeader(ApplicationContext context){
        this.context = context;
    }

    @Override
    public void run(){
        log.info("sleeping to allow other instances to spin up");
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            log.warn("leader election thread interrupted!");
        }
        log.trace("before getting sessions");
        WebSocketConnector webSocketConnector = context.getBean(WebSocketConnector.class);
        //get sessions to ensure that they are initialized
        List<StompSession> sessions = webSocketConnector.getSessions(context);
        log.info("sleeping to allow other instances to SUBSCRIBE");
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            log.warn("thread interrupted!");
        }
        LeaderElectionController leaderElectionController = context.getBean(LeaderElectionController.class);
        log.trace("before sending leader election message");
        leaderElectionController.sendLeaderElection();
    }
}
