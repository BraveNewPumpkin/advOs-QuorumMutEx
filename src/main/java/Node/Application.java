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

        log.trace("--------before running application");
        ApplicationContext context = SpringApplication.run(Application.class, args);
        Thread thread = new Thread(new ElectNewLeader(context));
        log.trace("--------before running sendLeaderElection thread");
        thread.start();
    }
}