package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Component
@Slf4j
public class DoStartCSRequester implements Runnable{
    private final WebSocketConnector webSocketConnector;
    private final QuorumMutExController quorumMutExController;
    private final CsRequester csRequester;
    private final Semaphore connectingSynchronizer;
    private final ThisNodeInfo thisNodeInfo;

    @Autowired
    public DoStartCSRequester(
            WebSocketConnector webSocketConnector,
            QuorumMutExController quorumMutExController,
            CsRequester csRequester,
            @Qualifier("Node/ConnectConfig/connectingSynchronizer")
            Semaphore connectingSynchronizer,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo

    ){
        this.webSocketConnector = webSocketConnector;
        this.quorumMutExController = quorumMutExController;
        this.csRequester = csRequester;
        this.connectingSynchronizer = connectingSynchronizer;
        this.thisNodeInfo = thisNodeInfo;
    }

    @Override
    public void run(){
        csRequester.startRequesting();
    }

}
