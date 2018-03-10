package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;

@Controller
@EnableAsync
@Slf4j
public class BfsTreeController {
    private final BfsTreeService bfsTreeService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;

    private final Object searchMonitor;
    private final Object readyToBuildMonitor;
    private final Object buildMonitor;

    @Autowired
    public BfsTreeController(
            BfsTreeService bfsTreeService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ){
        this.bfsTreeService = bfsTreeService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;

        searchMonitor = new Object();
        readyToBuildMonitor = new Object();
        buildMonitor = new Object();
    }


    @Async("clientInboundChannelExecutor")
    @MessageMapping("/bfsTreeSearch")
    public void bfsTreeSearch(BfsTreeSearchMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received bfs tree search message {}", message);
        }
        synchronized (searchMonitor) {
            bfsTreeService.search(message.getSourceUID(), message.getDistance());
       }
    }

    @MessageMapping("/bfsTreeAcknowledge")
    public void bfsTreeAcknowledge(BfsTreeAcknowledgeMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received bfs tree acknowledge message {}", message);
        }
        bfsTreeService.acknowledge(message.getSourceUID(), message.getTargetUid());
    }

    @MessageMapping("/bfsTreeReadyToBuild")
    public void bfsTreeReadyToBuild(BfsTreeReadyToBuildMessage message) {
        if(!bfsTreeService.isReadyToBuild()) {
            if (log.isDebugEnabled()) {
                log.debug("<---received bfs tree ready to build message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---ignoring bfs tree ready to build message {}", message);
            }
        }
        synchronized (readyToBuildMonitor) {
            bfsTreeService.buildReady();
        }
    }

    @MessageMapping("/bfsTreeBuild")
    public void bfsTreeBuild(BfsTreeBuildMessage message) {
        if (log.isDebugEnabled()) {
            if(message.getParentUid() == thisNodeInfo.getUid()) {
                log.debug("<---received bfs tree build message {}", message);
            }
        }
        synchronized (buildMonitor) {
            bfsTreeService.build(message.getParentUid(), message.getTree());
        }
    }

    public void sendBfsTreeSearch() throws MessagingException {
        BfsTreeSearchMessage message = new BfsTreeSearchMessage(
            thisNodeInfo.getUid(),
            bfsTreeService.getDistanceToNeighborFromRoot()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending BfsTreeSearch message: {}", message);
        }
        template.convertAndSend("/topic/bfsTreeSearch", message);
        log.trace("BfsTreeSearch message sent");
    }

    public void sendBfsTreeAcknowledge() throws MessagingException {
        BfsTreeAcknowledgeMessage message = new BfsTreeAcknowledgeMessage(
                thisNodeInfo.getUid(),
                bfsTreeService.getParentUID(),
                bfsTreeService.getThisDistanceFromRoot()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending BfsTreeAcknowledge message: {}", message);
        }
        template.convertAndSend("/topic/bfsTreeAcknowledge", message);
        log.trace("BfsTreeAcknowledge message sent");
    }

    public void sendBfsReadyToBuildMessage() throws MessagingException {
        BfsTreeReadyToBuildMessage message = new BfsTreeReadyToBuildMessage(
                thisNodeInfo.getUid()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending bfs ready to build message: {}", message);
        }
        template.convertAndSend("/topic/bfsTreeReadyToBuild", message);
        log.trace("BfsTreeReadyToBuildMessage message sent");
    }

    public void sendBfsBuildMessage() throws MessagingException {
        BfsTreeBuildMessage message = new BfsTreeBuildMessage(
                thisNodeInfo.getUid(),
                bfsTreeService.getParentUID(),
                bfsTreeService.getTree()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending bfs build message: {}", message);
        }
        template.convertAndSend("/topic/bfsTreeBuild", message);
        log.trace("BfsTreeBuildMessage message sent");
    }
}
