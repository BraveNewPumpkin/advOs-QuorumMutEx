package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@Slf4j
public class Application {
    public static void main(String[] args) {
        if(log.isTraceEnabled()) {
            log.trace("before running application");
        }
        ApplicationContext context = SpringApplication.run(Application.class, args);
        DoMapProtocol doMapProtocol = context.getBean(DoMapProtocol.class);
        Thread mapThread = new Thread(doMapProtocol);
        if(log.isTraceEnabled()) {
            log.trace("before running doMapProtocol thread");
        }
        mapThread.start();
        DoSnapshotProtocol doSnapshotProtocol = context.getBean(DoSnapshotProtocol.class);
        Thread snapshotThread = new Thread(doSnapshotProtocol);
        if(log.isTraceEnabled()) {
            log.trace("before running doSnapshotProtocol thread");
        }
        snapshotThread.start();
    }
}