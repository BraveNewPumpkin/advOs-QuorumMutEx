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
        //TODO: change to trace
        log.error("-------before sleep to allow other instances to spin up");
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            log.debug("thread interrupted!");
        }
        //TODO: change to trace
        log.error("--------before getting sessions");
        WebSocketConnector webSocketConnector = context.getBean(WebSocketConnector.class);
        List<StompSession> sessions = webSocketConnector.getSessions(context);
        //TODO: change to trace
        log.error("-------before sleep to allow other instances to SUBSCRIBE");
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            log.debug("thread interrupted!");
        }
        LeaderElectionController leaderElectionController = context.getBean(LeaderElectionController.class);
        //TODO: change to trace
        log.error("--------before sending leader election message");
        leaderElectionController.sendLeaderElection();
    }
}
