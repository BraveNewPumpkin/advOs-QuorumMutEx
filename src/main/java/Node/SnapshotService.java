package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class SnapshotService {
    private final SnapshotController snapshotController;
    private final ThisNodeInfo thisNodeInfo;
    private final MapInfo mapInfo;
    private SnapshotInfo snapshotInfo;
    private final TreeInfo treeInfo;
    private Tree<Integer> tree;
    private MessageIntRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer;
    private MessageIntRoundSynchronizer<StateMessage> snapshotStateSynchronizer;
    private final GateLock preparedForSnapshotSynchronizer;

    //protection for if we receive the last marker message from neighbors and last state message from children at the same time
    private final Object processingFinalStateOrMarkerSynchronizer;

    //isMarked booleans for each round
    private List<Boolean> isMarkedList;
    //used to save and defer sending state to parent if we have not yet received all marked messages this round
    private List<Boolean> isStateReadyToSend;
    private Map<Integer, Map<Integer, SnapshotInfo>> childrenStatesMaps;
    private Map<Integer, SnapshotInfo> thisNodeSnapshots;
    private final SnapshotReadWriter snapshotReadWriter;
    private boolean isTerminatedLastRound;

    @Autowired
    public SnapshotService(
        @Lazy SnapshotController snapshotController,
        SnapshotReadWriter snapshotReadWriter,
        @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
        @Qualifier("Node/MapConfig/mapInfo") MapInfo mapInfo,
        @Qualifier("Node/NodeConfigurator/snapshotInfo") SnapshotInfo snapshotInfo,
        @Qualifier("Node/BuildTreeConfig/treeInfo") TreeInfo treeInfo,
        @Qualifier("Node/BuildTreeConfig/tree") Tree<Integer> tree,
        @Qualifier("Node/SnapshotConfig/snaphshotMarkerSynchronizer")
        MessageIntRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer,
        @Qualifier("Node/SnapshotConfig/snaphshotStateSynchronizer")
        MessageIntRoundSynchronizer<StateMessage> snapshotStateSynchronizer,
        @Qualifier("Node/SnapshotConfig/preparedForSnapshotSynchronizer")
        GateLock preparedForSnapshotSynchronizer
    ) {
        this.snapshotController = snapshotController;
        this.snapshotReadWriter = snapshotReadWriter;
        this.thisNodeInfo = thisNodeInfo;
        this.mapInfo = mapInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;
        this.tree = tree;
        this.snapshotMarkerSynchronizer = snapshotMarkerSynchronizer;
        this.snapshotStateSynchronizer = snapshotStateSynchronizer;
        this.preparedForSnapshotSynchronizer = preparedForSnapshotSynchronizer;

        processingFinalStateOrMarkerSynchronizer = new Object();
        isMarkedList = new ArrayList<>();
        isStateReadyToSend = new ArrayList<>();
        childrenStatesMaps = new HashMap<>();
        thisNodeSnapshots = new HashMap<>();
        isTerminatedLastRound = false;
    }

    public synchronized void doMarkingThings(int markerRoundNumber) {
        if(thisNodeInfo.getUid() != 0) {
            //if we are leaf, send state to parent
            fillHasStatePendingToIndex(markerRoundNumber);
            synchronized (processingFinalStateOrMarkerSynchronizer) {
                Map<Integer, SnapshotInfo> snapshotInfos;
                snapshotInfos = combineSnapshotInfoMaps(new ArrayList<>(),snapshotMarkerSynchronizer.getRoundId());
                if (isLeaf() || isStateReadyToSend.get(markerRoundNumber)) {
                    if (!isLeaf()) {
                        log.debug("sending deferred stateMessage for round {}", markerRoundNumber);
                        snapshotInfos.putAll(childrenStatesMaps.get(markerRoundNumber));
                    } else {

                        snapshotStateSynchronizer.incrementRoundNumber();
                    }
                    snapshotController.sendStateMessage(snapshotInfos, markerRoundNumber);
                } else {
                    //save this node's state for when we receive from all children
//                    SnapshotInfo thisNodeSnapshot = snapshotInfos.get(thisNodeInfo.getUid());
//                    thisNodeSnapshots.put(markerRoundNumber, thisNodeSnapshot);
                }
            }
            snapshotMarkerSynchronizer.incrementRoundNumber();
        }
    }

    public synchronized void doStateThings(List<Map<Integer, SnapshotInfo>> snapshotInfoMaps, int snapshotNumber){
        Map<Integer, SnapshotInfo> snapshotInfos = combineSnapshotInfoMaps(snapshotInfoMaps, snapshotStateSynchronizer.getRoundId());
        if(thisNodeInfo.getUid() == 0) {
            if(!isTerminatedLastRound){
                printStates(snapshotInfos, snapshotNumber);
            }
            boolean isTerminated = terminationDetection(snapshotInfos);
            log.debug("Termination Detection: {}",isTerminated);
            isTerminatedLastRound = isTerminated;
        } else {
            synchronized (processingFinalStateOrMarkerSynchronizer) {
                int stateRoundNumber = snapshotStateSynchronizer.getRoundId();
                fillHasStatePendingToIndex(stateRoundNumber);
                //if we've received all the marker messages then send to parent, otherwise defer until we do receive all
                int numMarkerMessagesForStateRound = snapshotMarkerSynchronizer.getNumMessagesForGivenRound(stateRoundNumber);
                if (numMarkerMessagesForStateRound == snapshotMarkerSynchronizer.getRoundSize()){
                    SnapshotInfo thisNodeSnapshot = thisNodeSnapshots.get(stateRoundNumber);
                    snapshotInfos.put(thisNodeInfo.getUid(), thisNodeSnapshot);
                    snapshotController.sendStateMessage(snapshotInfos, stateRoundNumber);
                } else {
                    log.debug("did not have all marker message for round {}. deferring.", stateRoundNumber);
                    childrenStatesMaps.put(snapshotStateSynchronizer.getRoundId(), snapshotInfos);
                    isStateReadyToSend.set(snapshotStateSynchronizer.getRoundId(), true);
                }
            }
        }
        snapshotStateSynchronizer.incrementRoundNumber();
    }

    public boolean isMarked(int roundNumber) {
        boolean isMarked = false;
        if(isMarkedList.size() > roundNumber) {
            isMarked = isMarkedList.get(roundNumber);
        }
        return isMarked;
    }

    public void setIsMarked(int messageRoundNumber, boolean isMarkedVal){
        int isMarkedCounter= this.isMarkedList.size()-1;
        if( isMarkedCounter < messageRoundNumber){
            for(int i=isMarkedCounter;i<messageRoundNumber;i++){
                isMarkedList.add(false);
            }
        }
         this.isMarkedList.set(messageRoundNumber, isMarkedVal);
    }

    public synchronized void checkAndSendMarkerMessage(int messageRoundNumber, int sourceId, FifoRequestId fifoRequestId){
        if(thisNodeInfo.getUid() != 0) {
            preparedForSnapshotSynchronizer.enter();
            if (!isMarked(messageRoundNumber)) {
                saveState();
                setIsMarked(messageRoundNumber, true);
                snapshotController.sendMarkMessage(messageRoundNumber);
            }
        }
        snapshotController.sendFifoResponse(sourceId, fifoRequestId);
    }

    public void saveState(){
        SnapshotInfo snap = new SnapshotInfo();
        snap.setVectorClock(new ArrayList<>(thisNodeInfo.getVectorClock()));
        snap.setActive(mapInfo.isActive());
        snap.setSentMessages(snapshotInfo.getSentMessages());
        snap.setProcessedMessages(snapshotInfo.getProcessedMessages());
//        snapshotInfo.setVectorClock(thisNodeInfo.getVectorClock());
//        snapshotInfo.setActive(mapInfo.isActive());
        thisNodeSnapshots.put(snapshotMarkerSynchronizer.getRoundId(),snap);
        log.debug("In saveState(), round: {}   vectorClock: {}", snapshotMarkerSynchronizer.getRoundId(),thisNodeSnapshots.get(snapshotMarkerSynchronizer.getRoundId()).getVectorClock());
    }

    public Map<Integer, SnapshotInfo> combineSnapshotInfoMaps(List<Map<Integer, SnapshotInfo>> snapshotMaps, int roundNumber) {
        Map<Integer, SnapshotInfo> snapshotInfos = new HashMap<>();

        //just added, including the additional parameter
        SnapshotInfo snap=thisNodeSnapshots.get(roundNumber);
        snapshotInfos.put(thisNodeInfo.getUid(), snap);

//        snapshotInfos.put(thisNodeInfo.getUid(), snapshotInfo);
        snapshotMaps.forEach((snapshotMap) -> {
            snapshotInfos.putAll(snapshotMap);
        });
        return snapshotInfos;
    }

    public void fillHasStatePendingToIndex(int index) {
        if(index > isStateReadyToSend.size() - 1) {
            int numEntriesToAdd = index - isStateReadyToSend.size() + 1;
            isStateReadyToSend.addAll(Collections.nCopies(numEntriesToAdd, false));
        }
    }

    public boolean terminationDetection(Map<Integer,SnapshotInfo> snapshotInfos){
        int totalSentMessages=0;
        int totalProcessedMessages=0;
        boolean areStatesActive=false;
        boolean isConsistent=true;

        int n=thisNodeInfo.getTotalNumberOfNodes();
        int[][] snapshotMatrix = new int[n][n];

        Set entrySet = snapshotInfos.entrySet();
        Iterator it= entrySet.iterator();

        while(it.hasNext()){
            Map.Entry map=(Map.Entry) it.next();
            int key=(int)map.getKey();
            SnapshotInfo snapInfo = (SnapshotInfo)map.getValue();

            totalSentMessages+=snapInfo.getSentMessages();
            totalProcessedMessages+=snapInfo.getProcessedMessages();

            areStatesActive = areStatesActive || snapInfo.isActive();

            for(int i=0;i<n;i++){
                snapshotMatrix[key][i]=snapInfo.getVectorClock().get(i);
            }
        }

        for(int q=0;q<n;q++){
            int maxVal=0;
            for(int p=0;p<n;p++){
                int val=snapshotMatrix[p][q];
                if(maxVal<val)
                    maxVal=val;
            }
            if(maxVal != snapshotMatrix[q][q]){
                isConsistent=false;
                break;
            }
        }

        if(isConsistent)
            log.debug("The Snapshot is Consistent");
        else
            log.debug("The Snapshot is Not Consistent");

//        log.debug("Total Sent Messages = {} and Total Received Messages = {}", totalSentMessages,totalProcessedMessages);
//        log.debug("isActive: {}",areStatesActive);

//        if(!areStatesActive && totalSentMessages==(totalProcessedMessages-thisNodeInfo.getTotalNumberOfNodes()))
        if(!areStatesActive && totalSentMessages==(totalProcessedMessages-1))
            return true;
        else
            return false;
    }

    private boolean isLeaf() {
        log.trace("tree.getRoot().getChildren(): {}", tree.getRoot().getChildren());
        log.trace("tree.getRoot().getChildren().isEmpty(): {}", tree.getRoot().getChildren().isEmpty());
        log.trace("tree.getRoot().getChildren().size(): {}", tree.getRoot().getChildren().size());
        if(tree.getRoot().getChildren().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void printStates(Map<Integer, SnapshotInfo> snapshotInfos, int snapshotNumber) {
        if(log.isInfoEnabled()) {
            log.info("snapshot {}: {}", snapshotNumber, Arrays.toString(snapshotInfos.entrySet().toArray()));
        }
        snapshotReadWriter.writeSnapshots(snapshotInfos);
    }
}
