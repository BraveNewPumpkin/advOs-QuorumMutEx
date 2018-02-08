package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class RootController {
    @Autowired
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    ThisNodeInfo thisNodeInfo;

    @MessageMapping("/")
    @SendTo("/")
    public Message connect(ConnectMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay TODO: remove
        return new ConnectResponse(thisNodeInfo.getUid(), message.getSourceUID());
    }

}