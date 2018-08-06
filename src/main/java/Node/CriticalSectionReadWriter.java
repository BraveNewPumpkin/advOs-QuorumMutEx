package Node;

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

@Slf4j
@Component
public class CriticalSectionReadWriter {
    Path fileOutputPath;
    PrintWriter fileOutputWriter;


    @Autowired
    public CriticalSectionReadWriter(
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
                    ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/NodeConfigurator/configResource")
                    Resource configResource
    ) {
        String outputFileNamePrefix = com.google.common.io.Files.getNameWithoutExtension(configResource.getFilename()) + "-";
        String outputFileNameSuffix = ".out";
        int nodeUid = thisNodeInfo.getUid();
        String outputFileName = outputFileNamePrefix + nodeUid + outputFileNameSuffix;
        fileOutputPath = Paths.get("output", outputFileName);
        try {
            OutputStream fileOutputStream = Files.newOutputStream(fileOutputPath);
            fileOutputWriter = new PrintWriter(fileOutputStream);
            log.trace("opened {} for writing", fileOutputPath);
        } catch (java.io.IOException e) {
            log.error("failed to open {} for writing: {}", fileOutputPath.toAbsolutePath(), e.getMessage());
        }
    }

    public void writeCriticalSectionNumber(int criticalSectionNumber) {
        log.trace("attempting to write critical section number {}", criticalSectionNumber);
        fileOutputWriter.write(criticalSectionNumber + "\n");
        fileOutputWriter.flush();
    }

}