package Node;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NodeConfigurator {
    @Bean
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    public ThisNodeInfo getThisNodeInfo() {
        //TODO read from config
        int uid = 1;
        String hostName = "localhost";
        int port = 8080;

        ThisNodeInfo thisNodeInfo = new ThisNodeInfo(uid, hostName, port);

        thisNodeInfo.addNeighbor(new NodeInfo(2, "localhost", 8081));

        return thisNodeInfo;
    }

}
