package Node;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class TestCsOverlap {

    private Resource configResource;
    private final ThisNodeInfo thisNodeInfo;

    public TestCsOverlap(
    @Qualifier("Node/NodeConfigurator/configResource")
    Resource configResource,
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    ThisNodeInfo thisNodeInfo
    ){
        this.configResource=configResource;
        this.thisNodeInfo=thisNodeInfo;
    }

    public boolean checkForOverlap() {

        String outputFileNamePrefix = com.google.common.io.Files.getNameWithoutExtension(configResource.getFilename()) + "-";
//        String outputFileNamePrefix = "Config3.txt-";
        String outputFileNameSuffix = ".out";

        Path fileOutputPath;
        HashSet<Integer> criticalSections = new HashSet<>();

        boolean doesOverlap = false;

        for (int nodeUid = 0; nodeUid < thisNodeInfo.getTotalNumberOfNodes(); nodeUid++) {
            String outputFileName = outputFileNamePrefix + nodeUid + outputFileNameSuffix;
            fileOutputPath = Paths.get("output", outputFileName);
            String line;

            try (
                    InputStream is = Files.newInputStream(fileOutputPath);
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
            ) {

                while ((line = br.readLine()) != null) {
                    int csNum = Integer.parseInt(line);
                    if (criticalSections.contains(csNum)) {
                        doesOverlap = true;
                        break;
                    } else {
                        criticalSections.add(csNum);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (doesOverlap)
                break;
        }

        return doesOverlap;
    }
}
