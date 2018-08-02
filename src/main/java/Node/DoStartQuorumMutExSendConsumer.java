package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DoStartQuorumMutExSendConsumer implements Runnable {
    private final SimpMessagingTemplate template;
    private final QuorumMutExInfo quorumMutExInfo;
    private final BlockingQueue<QuorumMutExSendWork> workQueue;

    @Autowired
    public DoStartQuorumMutExSendConsumer(
            SimpMessagingTemplate template,
            QuorumMutExInfo quorumMutExInfo,
            @Qualifier("Node/QuorumMutExConfig/sendWorkQueue")
            BlockingQueue<QuorumMutExSendWork> workQueue
    ) {
        this.template = template;
        this.quorumMutExInfo = quorumMutExInfo;
        this.workQueue = workQueue;
    }

    @Override
    public void run() {
        while(true) {
            try {
                QuorumMutExSendWork quorumMutExSendWork = workQueue.take();
                NodeMessage message = quorumMutExSendWork.getMessage();
                String route = quorumMutExSendWork.getRoute();
                if(log.isDebugEnabled()){
                    String className = message.getClass().getSimpleName();
                    log.debug("--->sending {} message: {}", className, message);
                }
                template.convertAndSend(route, message);
                if(log.isTraceEnabled()) {
                    String className = message.getClass().getSimpleName();
                    log.trace("{} message sent", className);
                }

                quorumMutExInfo.incrementNumSentMessages();
                TimeUnit.MILLISECONDS.sleep(10);
            } catch(InterruptedException e) {
                //ignore
            }
        }
    }
}
