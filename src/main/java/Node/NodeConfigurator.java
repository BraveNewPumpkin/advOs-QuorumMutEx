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
        ThisNodeInfo thisNodeInfo = new ThisNodeInfo(
                thisUid,
                totalNumberOfNodes,
                thisHostName,
                thisPort,
                nodeConfig.minPerActive,
                nodeConfig.maxPerActive,
                nodeConfig.minSendDelay,
                nodeConfig.snapshotDelay,
                nodeConfig.maxNumber
        );

        nodeConfig.neighbors.forEach(neighborUid -> {
            NodeInfo neighbor = nodeConfig.nodes.get(neighborUid);
            thisNodeInfo.addNeighbor(neighbor);
        });

        return thisNodeInfo;
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/subscriptionDestinations")
    public List<String> getSubscriptionDestinations() {
        return Arrays.asList(
            "/topic/mapMessage"
        );
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/connectionTimeoutLatch")
    public CountDownLatch getConnectionTimeoutLatch() {
        return new CountDownLatch(1);
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/maxNumberSynchronizer")
    public Object getMaxNumberSynchronizer() {
        return new Object();
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/snapshotInfo")
    public SnapshotInfo getSnapshotInfo() {
        return new SnapshotInfo();
    }


    private NodeConfig readNodeConfig(ApplicationContext context, String thisNodeHostName) throws ConfigurationException {
        Resource resource = context.getResource(nodeConfigUri);
        Map<Integer, NodeInfo> nodes = new HashMap<>();
        List<Integer> neighbors = new ArrayList<>();
        String line;
        int count = 0;

        boolean validLine1=false;
        boolean nodeDetails=false;
        boolean neighborDetails=false;
        boolean hasThisNodeUidBeenFound = false;
        int thisNodeUid=0;
        int numOfNodes=0, minPerActive=0,maxPerActive=0, minSendDelay=0, snapshotDelay=0, maxNumber=0;

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

                if(words.peek().matches("\\D+")){
                    continue;
                }
                if(!validLine1 && words.size()>=6){
                    numOfNodes=Integer.parseInt(words.remove());
                    minPerActive=Integer.parseInt(words.remove());
                    maxPerActive=Integer.parseInt(words.remove());
                    minSendDelay=Integer.parseInt(words.remove());
                    snapshotDelay=Integer.parseInt(words.remove());
                    maxNumber=Integer.parseInt(words.remove());
                    validLine1=true;
                }
                else if(!nodeDetails && count<numOfNodes){
                    //read node details

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

                    if(count==numOfNodes) {
                        nodeDetails = true;
                        count=0; //reusing count variable for counting nodes while extracting neighbors
                    }
                }
                else if(!neighborDetails && count<numOfNodes){
                    //Read neighbors
                    if(!hasThisNodeUidBeenFound) {
                        throw new ConfigurationException("could not find node matching HOSTNAME");
                    }
                    if(count!=thisNodeUid) {
                        count++;
                        continue;
                    }


                    for(int i=0;i<words.size();i++){
                        String str=words.remove();

                        if(str.matches("\\d+")){
                            neighbors.add(Integer.parseInt(str));
                        }
                        else {
                            break;
                        }
                    }
                    count++;
                    if(count==numOfNodes)
                        neighborDetails=true;
                }
            }
        }catch(IOException e){
            log.error(e.getMessage());
        }
        return new NodeConfig(thisNodeUid, numOfNodes, nodes,minPerActive,maxPerActive,minSendDelay,snapshotDelay,maxNumber,neighbors);
    }

    private class NodeConfig {
        private int thisUid;
        private int totalNumberOfNodes;
        private Map<Integer, NodeInfo> nodes;
        private List<Integer> neighbors;
        private int minPerActive;
        private int maxPerActive;
        private int minSendDelay;
        private int snapshotDelay;
        private int maxNumber;


        public NodeConfig(
                int thisUid,
                int totalNumberOfNodes,
                Map<Integer, NodeInfo> nodes,
                int minPerActive,
                int maxPerActive,
                int minSendDelay,
                int snapshotDelay,
                int maxNumber,
                List<Integer> neighbors
        ) {
            this.thisUid = thisUid;
            this.totalNumberOfNodes = totalNumberOfNodes;
            this.nodes = nodes;
            this.minPerActive=minPerActive;
            this.maxPerActive=maxPerActive;
            this.minSendDelay=minSendDelay;
            this.snapshotDelay=snapshotDelay;
            this.maxNumber=maxNumber;
            this.neighbors = neighbors;
        }
    }
}
