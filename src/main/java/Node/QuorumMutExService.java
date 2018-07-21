package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class QuorumMutExService {
    private final QuorumMutExController quorumMutExController;
    private final ThisNodeInfo thisNodeInfo;
    private final QuorumMutExInfo quorumMutExInfo;

    private final Semaphore criticalSectionLock;

    @Autowired
    public QuorumMutExService(
            @Lazy QuorumMutExController quorumMutExController,
            QuorumMutExInfo quorumMutExInfo,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ) {
        this.quorumMutExController = quorumMutExController;
        this.thisNodeInfo = thisNodeInfo;
        this.quorumMutExInfo = quorumMutExInfo;

        criticalSectionLock = new Semaphore(0);
    }

    public void cs_enter() {
        //wait until we are allowed to enter cs
        quorumMutExController.sendRequestMessage();
        try {
            criticalSectionLock.acquire();
        } catch(java.lang.InterruptedException e) {
            //ignore
        }
    }

    public void cs_leave() {
        quorumMutExController.sendReleaseMessage();
    }
}