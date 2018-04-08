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
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@Slf4j
public class NodeConfigurator {
    @Value("${this.hostName:}")
    private String thisHostName;

    @Value("${this.isLocal:false}")
    private boolean isLocal;

    @Value("${nodeConfigUri:file:resources/config.txt}")
    private String nodeConfigUri;


    @Bean
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    public ThisNodeInfo getThisNodeInfo(
        @Autowired ApplicationContext context
    ) throws UnknownHostException, ConfigurationException {
        if (thisHostName.equals("")) {
            thisHostName = InetAddress.getLocalHost().getHostName();
        }

        //load config using thisHostName to know which node we are
        NodeConfig nodeConfig = readNodeConfig(context, thisHostName);

        //if trying to run locally, reset hostname to localhost
        if(isLocal) {
            thisHostName = "localhost";
        }

        int thisUid = nodeConfig.thisUid;
        int totalNumberOfNodes = nodeConfig.totalNumberOfNodes;
        int thisPort = nodeConfig.nodes.get(thisUid).getPort();
        ThisNodeInfo thisNodeInfo = new ThisNodeInfo(thisUid, totalNumberOfNodes, thisHostName, thisPort);
        thisNodeInfo.setComponentId(thisUid);

        nodeConfig.neighbors.forEach(neighborUid -> {
            NodeInfo neighbor = nodeConfig.nodes.get(neighborUid);
            thisNodeInfo.addNeighbor(neighbor);
            int edgeWeight = nodeConfig.distancesToNeighbors.get(neighbor);
            int biggerUid = Math.max(thisUid, neighborUid);
            int smallerUid = Math.min(thisUid, neighborUid);
            Edge edge = new Edge(biggerUid, smallerUid, edgeWeight);
            thisNodeInfo.addEdge(edge, neighbor);

        });

        return thisNodeInfo;
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/subscriptionDestinations")
    public List<String> getSubscriptionDestinations() {
        return Arrays.asList(
            "/topic/mwoeSearch",
            "/topic/mwoeResponse",
            "/topic/mwoeReject"
        );
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/connectionTimeoutLatch")
    public CountDownLatch getConnectionTimeoutLatch() {
        return new CountDownLatch(1);
    }

    private NodeConfig readNodeConfig(ApplicationContext context, String thisNodeHostName) throws ConfigurationException {
        Resource resource = context.getResource(nodeConfigUri);
        Map<Integer, NodeInfo> nodes = new HashMap<>();
        List<Integer> neighbors = new ArrayList<>();
        Map<NodeInfo, Integer> distancesToNeighbors = new HashMap<>();
        String line;
        int count = 0;

        int thisNodeUid = 0;
        boolean hasThisNodeUidBeenFound = false;
        int numberOfNodes = 0;
        boolean hasNumNodesBeenFound = false;
        Pattern edgeNodesPattern = Pattern.compile("\\((?<firstUid>\\d+),(?<secondUid>\\d+)\\)");
        try(
                InputStream is = resource.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ){
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.matches("^\\s*\\R")) {
                    continue;
                }
                Queue<String> words = new LinkedList<>(Arrays.asList(line.trim().split("\\s+")));
                if(words.size() == 0 || words.size() == 1 && words.peek().equals("")) {
                    continue;
                }
                if(!hasNumNodesBeenFound && words.size() == 1) {
                    hasNumNodesBeenFound = true;
                    numberOfNodes = Integer.parseInt(words.remove());
                    System.out.println(numberOfNodes);

                } else if(count < numberOfNodes) {
                    //parse nodes
                    int uid = Integer.parseInt(words.remove());
                    String hostName = words.remove();
                    int port = Integer.parseInt(words.remove());

                    if(thisNodeHostName.equals(hostName)){
                        hasThisNodeUidBeenFound = true;
                        thisNodeUid = uid;
                    }
                    if(isLocal) {
                        hostName = "localhost";
                    }
                    NodeInfo nodeInfo = new NodeInfo(uid, hostName, port);
                    nodes.put(uid, nodeInfo);
                    count++;
                } else if(count < numberOfNodes * 2) {
                    //parse neighbors

                    if(!hasThisNodeUidBeenFound) {
                        throw new ConfigurationException("could not find node matching HOSTNAME");
                    }
                    Matcher edgeNodesMatcher = edgeNodesPattern.matcher(words.remove());
                    if(!edgeNodesMatcher.matches()) {
                        throw new  ConfigurationException("could not match pattern for edges");
                    }
                    int firstUid = Integer.parseInt(edgeNodesMatcher.group("firstUid"));
                    int secondUid = Integer.parseInt(edgeNodesMatcher.group("secondUid"));
                    int otherUid = 0;
                    boolean isNeighbor = false;
                    if(thisNodeUid == firstUid) {
                        isNeighbor = true;
                        otherUid = secondUid;
                    } else if(thisNodeUid == secondUid){
                        isNeighbor = true;
                        otherUid = firstUid;
                    }
                    if(isNeighbor) {
                        neighbors.add(otherUid);
                        int distanceToNeighbor = Integer.parseInt(words.remove());
                        NodeInfo neighbor = nodes.get(otherUid);
                        distancesToNeighbors.put(neighbor, distanceToNeighbor);
                    }
                    count++;
                }
            }
        }catch(IOException e){
            log.error(e.getMessage());
        }
        System.out.println("List" + thisNodeUid+" "+numberOfNodes+" "+ nodes+" "+ neighbors+" "+ distancesToNeighbors);
        return new NodeConfig(thisNodeUid, numberOfNodes, nodes, neighbors, distancesToNeighbors);
    }

    private class NodeConfig {
        private int thisUid;
        private int totalNumberOfNodes;
        private Map<Integer, NodeInfo> nodes;
        private List<Integer> neighbors;
        private Map<NodeInfo, Integer> distancesToNeighbors;

        public NodeConfig(int thisUid,
                          int totalNumberOfNodes,
                          Map<Integer, NodeInfo> nodes,
                          List<Integer> neighbors,
                          Map<NodeInfo, Integer> distancesToNeighbors
                          ) {
            this.thisUid = thisUid;
            this.totalNumberOfNodes = totalNumberOfNodes;
            this.nodes = nodes;
            this.neighbors = neighbors;
            this.distancesToNeighbors = distancesToNeighbors;
        }
    }
}
