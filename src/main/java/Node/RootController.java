package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RootController {
    private SimpMessagingTemplate template;

    @Autowired
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    ThisNodeInfo thisNodeInfo;

    @Autowired
    public RootController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/")
    @SendTo("/")
    public Message connect(ConnectMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay TODO: remove
        //TODO: check round number vs current round number. if greater push onto queue
        return new ConnectResponse(thisNodeInfo.getUid(), message.getSourceUID());
    }

    //TODO make this a real messsage, connect isn't needed
    public void sendUID() throws MessagingException {
        thisNodeInfo.getNeighbors().parallelStream().forEach(neighbor -> {
            ConnectMessage message = new ConnectMessage(thisNodeInfo.getUid(), neighbor.getUid());
            template.convertAndSend("/topic", message);
        });
    }

}