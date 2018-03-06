package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BuildBfsTree implements Runnable{
    private final BfsTreeController bfsTreeController;
    private final BfsTreeService bfsTreeService;

    @Autowired
    public BuildBfsTree(
        BfsTreeController bfsTreeController,
        BfsTreeService bfsTreeService
    ){
        this.bfsTreeController = bfsTreeController;
        this.bfsTreeService = bfsTreeService;
    }

    @Override
    public void run(){
        //TODO move this work to a service level function
        log.trace("before sending bfs tree message");
        bfsTreeService.setRootNode(true);
        bfsTreeService.setMarked(true);
        bfsTreeController.sendBfsTreeSearch();
    }
}
