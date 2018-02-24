package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Slf4j
public class NodeConfigurator {
    @Value("${this.port:3332}")
    private int thisPort;

    @Value("${neighbor.port:5678}")
    private int neighborPort;

    @Value("${this.uid:123}")
    private int thisUid;

    @Bean
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    public ThisNodeInfo getThisNodeInfo() throws UnknownHostException{
        String hostName = InetAddress.getLocalHost().getHostName();
        //TODO remove
        log.error("hostname: " + hostName);

        //TODO read from config
        hostName = "localhost";

        ThisNodeInfo thisNodeInfo = new ThisNodeInfo(thisUid, hostName, thisPort);

        thisNodeInfo.addNeighbor(new NodeInfo(5, "localhost", neighborPort));

        return thisNodeInfo;
    }

}
