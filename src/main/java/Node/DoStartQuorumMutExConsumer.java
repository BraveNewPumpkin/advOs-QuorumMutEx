package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

@Component
@Slf4j
public class DoStartQuorumMutExConsumer implements Runnable {
    private final PriorityBlockingQueue<QuorumMutExWork> workQueue;

    @Autowired
    public DoStartQuorumMutExConsumer(
            @Qualifier("Node/QuorumMutExConfig/workQueue")
            PriorityBlockingQueue<QuorumMutExWork> workQueue
    ) {
        this.workQueue = workQueue;
    }

    @Override
    public void run() {
        while(true) {
            try {
                QuorumMutExWork quorumMutExWork = workQueue.take();
                quorumMutExWork.getWork().run();
            } catch(java.lang.InterruptedException e) {
                //ignore
            }
        }
    }
}
