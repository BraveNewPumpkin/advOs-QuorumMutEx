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
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                log.info("thread interrupted!");
            }
            List<StompSession> sessions = webSocketConnector.getSessions();
            RootController rootController = context.getBean(RootController.class);
            rootController.sendLeaderElection();
        });
        thread.start();
    }
}