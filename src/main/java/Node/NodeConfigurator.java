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
import java.util.concurrent.Semaphore;

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
    @Qualifier("Node/NodeConfigurator/configResource")
    public Resource getConfigResource(
        @Autowired ApplicationContext context
    ) {
        Resource configResource = context.getResource(nodeConfigUri);
        return configResource;
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/nodeConfig")
    public NodeConfig getNodeConfig(
        @Qualifier("Node/NodeConfigurator/configResource")
        Resource configResource
    ) throws UnknownHostException, ConfigurationException {
        if (thisHostName.equals("")) {
            thisHostName = InetAddress.getLocalHost().getHostName();
        }

        //load config using thisHostName to know which node we are
        NodeConfig nodeConfig = readNodeConfig(thisHostName, configResource);

        return nodeConfig;
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    public ThisNodeInfo getThisNodeInfo(
        @Qualifier("Node/NodeConfigurator/nodeConfig")
        NodeConfig nodeConfig){

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
                nodeConfig.numberOfRequests
        );

        nodeConfig.quorum.forEach(neighborUid -> {
            NodeInfo neighbor = nodeConfig.nodes.get(neighborUid);
            thisNodeInfo.addNeighbor(neighbor);

        });

        return thisNodeInfo;
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/csRequester")
    public CsRequesterInfo getCsRequesterInfo(
        @Qualifier("Node/NodeConfigurator/nodeConfig")
        NodeConfig nodeConfig){
        CsRequesterInfo csRequester = new CsRequesterInfo(
          nodeConfig.meanInterRequestDelay,
          nodeConfig.meanCsExecutionTime
        );

        return csRequester;
    }

    @Bean
    @Qualifier("Node/NodeConfigurator/sendingFifoSynchronizer")
    public Semaphore getSendingFifoSynchronizer() {
        return new Semaphore(1, true);
    }

    private NodeConfig readNodeConfig(
            String thisNodeHostName,
            Resource configResource
        ) throws ConfigurationException {
        Map<Integer, NodeInfo> nodes = new HashMap<>();
        List<Integer> quorum = new ArrayList<>();
        String line;
        int count = 0;

        boolean validLine1=false;
        boolean nodeDetails=false;
        boolean quorumDetails=false;
        boolean hasThisNodeUidBeenFound = false;
        int thisNodeUid=0;
        int numOfNodes=0, meanInterRequestDelay=0,meanCsExecutionTime=0, numberOfRequests=0;

        try(
                InputStream is = configResource.getInputStream();
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
                    meanInterRequestDelay=Integer.parseInt(words.remove());
                    meanCsExecutionTime=Integer.parseInt(words.remove());
                    numberOfRequests=Integer.parseInt(words.remove());
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
                else if(!quorumDetails && count<numOfNodes){
                    //Read neighbors
                    if(!hasThisNodeUidBeenFound) {
                        throw new ConfigurationException("could not find node matching HOSTNAME");
                    }
                    if(count!=thisNodeUid) {
                        count++;
                        continue;
                    }

                    int size=words.size();
                    for(int i=0;i<size;i++){
                        String str=words.remove();

                        if(str.matches("\\d+")){
                            quorum.add(Integer.parseInt(str));
                        }
                        else {
                            break;
                        }
                    }
                    count++;
                    if(count==numOfNodes)
                        quorumDetails=true;
                }
            }
        }
        catch(IOException e){
            log.error(e.getMessage());
        }

        return new NodeConfig(thisNodeUid, numOfNodes, nodes, meanInterRequestDelay, meanCsExecutionTime, numberOfRequests, quorum);
    }

    private class NodeConfig {
        private int thisUid;
        private int totalNumberOfNodes;
        private Map<Integer, NodeInfo> nodes;
        private List<Integer> quorum;
        private int meanInterRequestDelay;
        private int meanCsExecutionTime;
        private int numberOfRequests;



        public NodeConfig(
                int thisUid,
                int totalNumberOfNodes,
                Map<Integer, NodeInfo> nodes,
                int meanInterRequestDelay,
                int meanCsExecutionTime,
                int numberOfRequests,
                List<Integer> quorum
        ) {
            this.thisUid = thisUid;
            this.totalNumberOfNodes = totalNumberOfNodes;
            this.nodes = nodes;
            this.meanInterRequestDelay=meanInterRequestDelay;
            this.meanCsExecutionTime=meanCsExecutionTime;
            this.numberOfRequests=numberOfRequests;
            this.quorum = quorum;
        }
    }
}
