package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.List;

@SpringBootApplication
@Slf4j
public class Application {
    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(Application.class, args);
        WebSocketConnector webSocketConnector = context.getBean(WebSocketConnector.class);
        Thread thread = new Thread(() -> {
            log.trace("before sleep to allow other instances to spin up");
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                log.debug("thread interrupted!");
            }
            log.trace("before getting sessions");
            List<StompSession> sessions = webSocketConnector.getSessions();
            RootController rootController = context.getBean(RootController.class);
            log.trace("before sending leader election message");
            rootController.sendLeaderElection();
        });
        log.trace("before running sendLeaderElection thread");
        thread.start();
    }
}