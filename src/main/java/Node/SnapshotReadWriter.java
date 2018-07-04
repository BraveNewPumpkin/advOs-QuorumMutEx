package Node;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SnapshotReadWriter {
    Map<Integer, Path> fileOutputPaths;
    Map<Integer, PrintWriter> fileOutputWriters;


    @Autowired
    public SnapshotReadWriter(
        @Qualifier("Node/NodeConfigurator/thisNodeInfo")
        ThisNodeInfo thisNodeInfo,
        @Qualifier("Node/NodeConfigurator/configResource")
        Resource configResource
    ) {
        if(thisNodeInfo.getUid() == 0) {
            String outputFileNamePrefix = com.google.common.io.Files.getNameWithoutExtension(configResource.getFilename()) + "-";
            String outputFileNameSuffix = ".out";
            fileOutputPaths = new HashMap<>();
            fileOutputWriters = new HashMap<>();
            thisNodeInfo.getAllNodeUids().forEach((nodeUid) -> {
                String outputFileName = outputFileNamePrefix + nodeUid + outputFileNameSuffix;
                Path outputFilePath = Paths.get("output", outputFileName);
                fileOutputPaths.put(nodeUid, outputFilePath);
                try {
                    OutputStream fileOutputStream = Files.newOutputStream(outputFilePath);
                    PrintWriter fileOutputWriter = new PrintWriter(fileOutputStream);
                    fileOutputWriters.put(nodeUid, fileOutputWriter);
                    log.trace("opened {} for writing", outputFilePath);
                } catch (java.io.IOException e) {
                    log.error("failed to open {} for writing: {}", outputFilePath.toAbsolutePath(), e.getMessage());
                }
            });
        }
    }

    public void writeSnapshots(Map<Integer, SnapshotInfo> snapshotInfos) {
        snapshotInfos.forEach((uid, snapshotInfo) -> {
            log.trace("attempting to write snapshot for node {}", uid);
            log.trace("text to be written: {}", snapshotInfo.getVectorClock().toString());
            PrintWriter fileOutputWriter = fileOutputWriters.get(uid);
            String vectorClockString = formatVectorClock(snapshotInfo.getVectorClock());
            fileOutputWriter.write(vectorClockString + "\n");
            fileOutputWriter.flush();
        });
    }

    public String formatVectorClock(List<Integer> vectorClock) {
        return Joiner.on(" ").join(vectorClock);
    }
}
