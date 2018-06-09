package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Component
@Slf4j
public class DoSnapshotProtocol implements Runnable {
    private Semaphore connectingSynchronizer;
    private final ThisNodeInfo thisNodeInfo;

    private final ScheduledExecutorService scheduler;
    private final Runnable doSnapshot;

    @Autowired
    public DoSnapshotProtocol(
            @Qualifier("Node/MapConfig/connectingSynchronizer")
            Semaphore connectingSynchronizer,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/SnapshotConfig/scheduler")
            ScheduledExecutorService scheduler
    ){
        this.connectingSynchronizer = connectingSynchronizer;
        this.thisNodeInfo = thisNodeInfo;

        this.scheduler = scheduler;
        doSnapshot = ()->{
            //TODO
        };
    }

    @Override
    public void run(){
        try {
            connectingSynchronizer.acquire();
            //TODO do the snapshot stuff
            final ScheduledFuture<?> snapshotHandle = scheduler.scheduleAtFixedRate(doSnapshot, 0, thisNodeInfo.getSnapshotDelay(), MILLISECONDS);
        }catch (java.lang.InterruptedException e){
            //ignore
        }
    }
}
