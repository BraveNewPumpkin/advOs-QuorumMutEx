package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.naming.ConfigurationException;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Configuration
@Slf4j
public class NodeConfigurator {
    @Value("${this.hostName:'localhost'}")
    private String thisHostName;

    @Value("${nodeConfigUri:'file:resources/config.txt'}")
    private String nodeConfigUri;

    @Bean
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    public ThisNodeInfo getThisNodeInfo(
        @Autowired ApplicationContext context
    ) throws UnknownHostException, ConfigurationException {
//        String hostName = InetAddress.getLocalHost().getHostName();

        NodeConfig nodeConfig = readNodeConfig(context, thisHostName);
        //TODO remove this hardcoding
        thisHostName = "localhost";

        int thisUid = nodeConfig.thisUid;
        int thisPort = nodeConfig.nodes.get(thisUid).getPort();
        ThisNodeInfo thisNodeInfo = new ThisNodeInfo(thisUid, thisHostName, thisPort);

        nodeConfig.neighbors.forEach(neighborUid -> {
            NodeInfo neighbor = nodeConfig.nodes.get(neighborUid);
            thisNodeInfo.addNeighbor(neighbor);
        });

        return thisNodeInfo;
    }

    private NodeConfig readNodeConfig(ApplicationContext context, String thisNodeHostName) throws ConfigurationException {
        Resource resource = context.getResource(nodeConfigUri);
        Map<Integer, NodeInfo> nodes = new HashMap<>();
        List<Integer> neighbors = new ArrayList<>();
        String line;
        int count = 0;
        Integer thisNodeUid = null;
        try(
                InputStream is = resource.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ){
            //throw away first line
            br.readLine();
            int numberOfNodes = Integer.parseInt(br.readLine());
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#") && !line.matches("^\\s*\\R")) {
                    Queue<String> words = new LinkedList<>(Arrays.asList(line.trim().split("\\s+")));
                    if(words.size() == 0 || words.size() == 1 && words.peek().equals("")) {
                        continue;
                    }
                    if(count < numberOfNodes){
                        int uid = Integer.parseInt(words.remove());
                        String hostName = words.remove();
                        int port = Integer.parseInt(words.remove());

                        NodeInfo nodeInfo = new NodeInfo(uid, hostName, port);
                        nodes.put(uid, nodeInfo);
                        if(thisNodeHostName.equals(hostName)){
                            thisNodeUid = uid;
                        }
                        count++;
                    }

                    if(count > numberOfNodes && thisNodeUid == null) {
                        throw new ConfigurationException("could not find node matching HOSTNAME");
                    }

                    if(
                        count > numberOfNodes
                        && count <= numberOfNodes * 2
                        && thisNodeUid.equals(Integer.parseInt(words.remove()))
                    ) {
                        for (String word : words) {
                            neighbors.add(Integer.parseInt(word));
                        }
                    }
                }
            }
        }catch(IOException e){
            log.error(e.getMessage());
        }
        return new NodeConfig(thisNodeUid, nodes, neighbors);
    }

    private class NodeConfig {
        private int thisUid;
        private Map<Integer, NodeInfo> nodes;
        private List<Integer> neighbors;

        public NodeConfig(int thisUid, Map<Integer, NodeInfo> nodes, List<Integer> neighbors) {
            this.thisUid = thisUid;
            this.nodes = nodes;
            this.neighbors = neighbors;
        }
    }
}
