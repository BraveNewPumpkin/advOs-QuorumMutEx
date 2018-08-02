package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DoStartQuorumMutExInputConsumer implements Runnable {
    private final BlockingQueue<QuorumMutExInputWork> workQueue;

    @Autowired
    public DoStartQuorumMutExInputConsumer(
            @Qualifier("Node/QuorumMutExConfig/inputWorkQueue")
            BlockingQueue<QuorumMutExInputWork> workQueue
    ) {
        this.workQueue = workQueue;
    }

    @Override
    public void run() {
        while(true) {
            try {
                QuorumMutExInputWork quorumMutExInputWork = workQueue.take();
                quorumMutExInputWork.getWork().run();
                TimeUnit.MILLISECONDS.sleep(10);
            } catch(java.lang.InterruptedException e) {
                //ignore
            }
        }
    }
}
