package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Collections;


@Service
@Slf4j
public class MapService {
    private final MapController mapController;
    private final ThisNodeInfo thisNodeInfo;

    private int parentUid;
    private boolean isSearched;
    private int phaseNumber;

    @Autowired
    public MapService(
            @Lazy MapController mapController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ) {
        this.mapController = mapController;
        this.thisNodeInfo = thisNodeInfo;

        isSearched = false;
        this.phaseNumber =0;

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