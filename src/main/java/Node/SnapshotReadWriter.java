package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;

import java.util.Map;

public class SnapshotReadWriter {
    Map<Integer, String> outputFilePaths;

    @Autowired
    public SnapshotReadWriter(
        NodeConfigurator nodeConfigurator,
        @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ) {
        Resource configResource = nodeConfigurator.getConfigResource();
        String outputFileNamePrefix = configResource.getFilename() + "-";
        String outputFileNameSuffix = ".out";
        thisNodeInfo.getNeighbors().forEach((neighbor) -> {
            String outputFileName = outputFileNamePrefix + neighbor.getUid() + outputFileNameSuffix;
            outputFilePaths.put(neighbor.getUid(), outputFileName);
        });
    }

    public void writeSnapshots(Map<Integer, SnapshotInfo> snapshotInfo) {

    }
}
