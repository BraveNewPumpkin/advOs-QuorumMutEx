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
    private BuildTreeController buildTreeController;
    private BuildTreeService buildTreeService;
    private SnapshotController snapshotController;
    private final ThisNodeInfo thisNodeInfo;

    private final ScheduledExecutorService scheduler;
    private final Runnable doSnapshot;

    @Autowired
    public DoSnapshotProtocol(
            BuildTreeController buildTreeController,
            BuildTreeService buildTreeService,
            SnapshotController snapshotController,
            @Qualifier("Node/MapConfig/connectingSynchronizer")
            Semaphore connectingSynchronizer,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/SnapshotConfig/scheduler")
            ScheduledExecutorService scheduler
    ){
        this.connectingSynchronizer = connectingSynchronizer;
        this.buildTreeController = buildTreeController;
        this.buildTreeService = buildTreeService;
        this.snapshotController = snapshotController;
        this.thisNodeInfo = thisNodeInfo;
        this.scheduler = scheduler;

        doSnapshot = ()->{
            this.snapshotController.sendMarkMessage();
        };
    }

    @Override
    public void run(){
        try {
            connectingSynchronizer.acquire();
            log.trace("doing snapshot protocol");
            //ONLY SCHEDULE IF NODE ZERO
            if(thisNodeInfo.getUid() == 0) {
                buildTreeService.setParent(0);
                log.trace("we are root so sending initial buildTreeQueryMessage");
                buildTreeController.sendBuildTreeQueryMessage();
                log.trace("after sending build tree query message");
                final ScheduledFuture<?> snapshotHandle = scheduler.scheduleAtFixedRate(doSnapshot, 0, thisNodeInfo.getSnapshotDelay(), MILLISECONDS);
            }
        }catch (java.lang.InterruptedException e){
            //ignore
        }
    }
}
