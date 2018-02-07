import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class RootController {

    int thisUID;

    @MessageMapping("/")
    @SendTo("/")
    public Greeting greeting(ConnectMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new ConnectResponse(thisUID, message.getSourceUID());
    }

}