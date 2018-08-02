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

        DoConnect doConnect = context.getBean(DoConnect.class);
        Thread connectThread = new Thread(doConnect);
        if(log.isTraceEnabled()) {
            log.trace("before running connect thread");
        }
        connectThread.start();

        DoStartCSRequester doStartCSRequester = context.getBean(DoStartCSRequester.class);
        Thread csRequesterThread = new Thread(doStartCSRequester);
        if(log.isTraceEnabled()) {
            log.trace("before running csRequester thread");
        }
        csRequesterThread.start();

        DoStartQuorumMutExInputConsumer doStartQuorumMutExInputConsumer = context.getBean(DoStartQuorumMutExInputConsumer.class);
        Thread quorumMutExInputConsumerThread = new Thread(doStartQuorumMutExInputConsumer);
        if(log.isTraceEnabled()) {
            log.trace("before running quorumMutExInputConsumer thread");
        }
        quorumMutExInputConsumerThread.start();

        DoStartQuorumMutExSendConsumer doStartQuorumMutExSendConsumer = context.getBean(DoStartQuorumMutExSendConsumer.class);
        Thread quorumMutExSendConsumerThread = new Thread(doStartQuorumMutExSendConsumer);
        if(log.isTraceEnabled()) {
            log.trace("before running quorumMutExSendConsumer thread");
        }
        quorumMutExSendConsumerThread.start();
    }
}