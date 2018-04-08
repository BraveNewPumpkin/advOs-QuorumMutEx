package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SynchGhsService {
    private final SynchGhsController synchGhsController;
    private final ThisNodeInfo thisNodeInfo;

    private int parentUid;
    private boolean isSearched;

    @Autowired
    public SynchGhsService(
            @Lazy SynchGhsController synchGhsController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ) {
        this.synchGhsController = synchGhsController;
        this.thisNodeInfo = thisNodeInfo;

        isSearched = false;
    }

    public void mwoeIntraComponentSearch(int sourceUid, int componentId) {
        //TODO save the node from which i received first search
        parentUid = sourceUid;
        isSearched = true;
        synchGhsController.sendMwoeSearch();
    }

    public void mwoeInterComponentSearch(int sourceUid, int componentId) {
        //TODO implement
    }

    public boolean isFromComponentNode(int componentId) {
        return thisNodeInfo.getComponentId() == componentId;
    }

    public void markAsSearched() {
        isSearched = true;
    }


    public boolean isSearched() {
        return isSearched;
    }
}
