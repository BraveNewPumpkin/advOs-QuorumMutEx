package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class BfsTreeController {
    private BfsTreeService bfsTreeService;
    private SimpMessagingTemplate template;
    private ThisNodeInfo thisNodeInfo;

    @Autowired
    public BfsTreeController(
            BfsTreeService bfsTreeService,
            SimpMessagingTemplate template,
            ThisNodeInfo thisNodeInfo
    ){
        this.bfsTreeService = bfsTreeService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
    }


    @MessageMapping("/bfsTree")
    public void bfsTree(BfsTreeMessage message) {
        //TODO process message, use service, build tree
    }

    public void sendBfsTree() throws MessagingException {
        //TODO: change to trace
        log.error("--------creating bfs Tree message");
        BfsTreeMessage message = new BfsTreeMessage(
                thisNodeInfo.getUid()
        );
        template.convertAndSend("/topic/bfsTree", message);
        //TODO: change to trace
        log.error("--------after sending bfs tree message");
    }
}
