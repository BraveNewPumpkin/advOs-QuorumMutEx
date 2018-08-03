package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
public class FifoController {
    private final SimpMessagingTemplate template;
    private final QuorumMutExInfo quorumMutExInfo;
    private final ThisNodeInfo thisNodeInfo;
    private final Queue<Runnable> fifoWork;
    private final Semaphore sendingFifoSynchronizer;
    private FifoRequestId currentFifoRequestId;

    @Autowired
    public FifoController(
            SimpMessagingTemplate template,
            QuorumMutExInfo quorumMutExInfo,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/FifoConfig/fifoWork")
            Queue<Runnable> fifoWork
    ) {
        this.template = template;
        this.quorumMutExInfo = quorumMutExInfo;
        this.thisNodeInfo = thisNodeInfo;
        this.fifoWork = fifoWork;
        sendingFifoSynchronizer = new Semaphore(1);
    }

    private void createNewFifoRequestId(String salt) {
        currentFifoRequestId = new FifoRequestId(thisNodeInfo.getUid() + salt + quorumMutExInfo.getNumSentMessages());
    }

    @MessageMapping("/fifoResponseMessage")
    public void receiveFifoResponseMessage(FifoResponseMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received FifoResponse message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received FifoResponse message {}", message);
            }
        }
        sendingFifoSynchronizer.release();
    }

    public void sendFifoResponse(int targetUid, int sourceScalarClock, int sourceCriticalSectionNumber) throws MessagingException {
        final FifoResponseMessage message = new FifoResponseMessage(
                thisNodeInfo.getUid(),
                targetUid,
                sourceScalarClock,
                sourceCriticalSectionNumber,
                currentFifoRequestId
        );

        if(log.isDebugEnabled()){
            log.debug("--->sending fifoResponseMessage: {}", message);
        }
        template.convertAndSend("/topic/fifoResponseMessage", message);
//        sendFifo(message, "/topic/fifoResponseMessage");
        log.trace("fifoResponseMessage message sent");
    }

    public void sendFifo(FifoRequest message, String route) {
        Runnable sendFifoWork = () -> {
            try {
                sendingFifoSynchronizer.acquire();
                final String className = message.getClass().getSimpleName();
                createNewFifoRequestId(className);
                message.setFifoRequestId(currentFifoRequestId);
                if (log.isDebugEnabled()) {
                    log.debug("--->sending {} message: {}", className, message);
                }
                template.convertAndSend(route, message);
                if (log.isTraceEnabled()) {
                    log.trace("{} message sent", className);
                }

                quorumMutExInfo.incrementNumSentMessages();
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                //ignore
            }
        };
        fifoWork.add(sendFifoWork);
    }
}
