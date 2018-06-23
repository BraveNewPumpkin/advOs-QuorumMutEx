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
    private GateLock buildingTreeSynchronizer;
    private BuildTreeController buildTreeController;
    private BuildTreeService buildTreeService;
    private final SnapshotController snapshotController;
    private final SnapshotService snapshotService;
    private final ThisNodeInfo thisNodeInfo;
    private SnapshotInfo snapshotInfo;

    private final ScheduledExecutorService scheduler;

    @Autowired
    public DoSnapshotProtocol(
            BuildTreeController buildTreeController,
            BuildTreeService buildTreeService,
            SnapshotController snapshotController,
            SnapshotService snapshotService,
            @Qualifier("Node/MapConfig/connectingSynchronizer")
            Semaphore connectingSynchronizer,
            @Qualifier("Node/MapConfig/buildingTreeSynchronizer")
            GateLock buildingTreeSynchronizer,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/NodeConfigurator/snapshotInfo")
            SnapshotInfo snapshotInfo,
            @Qualifier("Node/SnapshotConfig/scheduler")
            ScheduledExecutorService scheduler
    ){
        this.connectingSynchronizer = connectingSynchronizer;
        this.buildingTreeSynchronizer = buildingTreeSynchronizer;
        this.buildTreeController = buildTreeController;
        this.buildTreeService = buildTreeService;
        this.snapshotController = snapshotController;
        this.snapshotService = snapshotService;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;
        this.scheduler = scheduler;
    }

    @Override
    public void run(){
        try {
            connectingSynchronizer.acquire();
            //ONLY SCHEDULE IF NODE ZERO
            if(thisNodeInfo.getUid() == 0) {
                log.trace("building tree then starting snapshot protocol");
                buildTreeService.setParent(0);
                log.trace("we are root so sending initial buildTreeQueryMessage");
                buildTreeController.sendBuildTreeQueryMessage();
                log.trace("waiting for tree to be built to start snapshots");
                buildingTreeSynchronizer.enter();
                snapshotService.setIsMarked(true);
                final ScheduledFuture<?> snapshotHandle = scheduler.scheduleAtFixedRate(snapshotController::sendMarkMessage, 0, thisNodeInfo.getSnapshotDelay(), MILLISECONDS);
            }
        }catch (java.lang.InterruptedException e){
            //ignore
        }
    }
}
