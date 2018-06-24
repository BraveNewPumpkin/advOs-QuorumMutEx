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
    private final Semaphore connectingSynchronizer;
    private final GateLock buildingTreeSynchronizer;
    private final BuildTreeController buildTreeController;
    private final BuildTreeService buildTreeService;
    private final SnapshotController snapshotController;
    private final SnapshotService snapshotService;
    private final ThisNodeInfo thisNodeInfo;
    private final SnapshotInfo snapshotInfo;
    private final TreeInfo treeInfo;
    private final NodeMessageRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer;
    private final NodeMessageRoundSynchronizer<StateMessage> snapshotStateSynchronizer;
    private final GateLock preparedForSnapshotSynchronizer;
    private final ScheduledExecutorService scheduler;

    private final Runnable doStartASnapshot;

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
            @Qualifier("Node/BuildTreeConfig/treeInfo")
            TreeInfo treeInfo,
            @Qualifier("Node/SnapshotConfig/snaphshotMarkerSynchronizer")
            NodeMessageRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer,
            @Qualifier("Node/SnapshotConfig/snaphshotStateSynchronizer")
            NodeMessageRoundSynchronizer<StateMessage> snapshotStateSynchronizer,
            @Qualifier("Node/SnapshotConfig/preparedForSnapshotSynchronizer")
            GateLock preparedForSnapshotSynchronizer,
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
        this.treeInfo = treeInfo;
        this.snapshotMarkerSynchronizer = snapshotMarkerSynchronizer;
        this.snapshotStateSynchronizer = snapshotStateSynchronizer;
        this.preparedForSnapshotSynchronizer = preparedForSnapshotSynchronizer;
        this.scheduler = scheduler;

        doStartASnapshot = () -> {
            snapshotController.sendMarkMessage(snapshotMarkerSynchronizer.getRoundNumber());
            snapshotService.setIsMarked(snapshotMarkerSynchronizer.getRoundNumber(), true);
            snapshotMarkerSynchronizer.incrementRoundNumber();
        };
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
            }
            buildingTreeSynchronizer.enter();
            int numberOfChildren = treeInfo.getNumChildren();
            snapshotStateSynchronizer.setRoundSize(numberOfChildren);
            preparedForSnapshotSynchronizer.open();
            if(thisNodeInfo.getUid() == 0) {
                final ScheduledFuture<?> snapshotHandle = scheduler.scheduleAtFixedRate(doStartASnapshot, 0, thisNodeInfo.getSnapshotDelay(), MILLISECONDS);
            }
        }catch (java.lang.InterruptedException e){
            //ignore
        }
    }
}
