package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MapService {
    private final MapController mapController;
    private final ThisNodeInfo thisNodeInfo;

    private Object maxNumberSynchronizer;
    private MapInfo mapInfo;

    @Autowired
    public MapService(
            @Lazy MapController mapController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ) {
        this.mapController = mapController;
        this.thisNodeInfo = thisNodeInfo;

        maxNumberSynchronizer = new Object();
        mapInfo = new MapInfo();
    }

    public void doActiveThings(){
        synchronized (maxNumberSynchronizer) {
            if(mapInfo.getMessagesSent() < thisNodeInfo.getMaxNumber()) {
                mapInfo.setActive(true);
                //TODO
                //pick random number of messages to send between minPerActive and maxPerActive
                //loop send message(s)
                    //check max number
                    //increment current number sent
                    //wait minSendDelay

                //set passive
                mapInfo.setActive(false);
            }
        }
    }

    public NodeInfo chooseRandomNeighbor(){
        //TODO implement
        NodeInfo chosenNeighbor;
        return chosenNeighbor;
    }

//    public void mwoeIntraComponentSearch(int sourceUid) {
//        parentUid = sourceUid;
//        isSearched = true;
//        mapController.sendMwoeSearch(false);
//    }

    public void markAsSearched() {
        isSearched = true;
    }

    public void markAsUnSearched() {
        isSearched = false;
    }

    public boolean isSearched() {
        return isSearched;
    }

    public int getParentUid() {
        return parentUid;
    }

}