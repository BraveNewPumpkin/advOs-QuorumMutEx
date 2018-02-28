package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Component
@Slf4j
public class DoLeaderElectionAndBfsTree implements Runnable{
    private final LeaderElectionService.Vote vote;
    private final Semaphore electingNewLeader;
    private final ApplicationContext context;
    private final ElectNewLeader electNewLeader;
    private final BuildBfsTree buildBfsTree;

    @Autowired
    public DoLeaderElectionAndBfsTree(
            ApplicationContext context,
            @Qualifier("Node/LeaderElectionConfig/electingNewLeader")
            Semaphore electingNewLeader,
            @Qualifier("Node/LeaderElectionService/vote")
            LeaderElectionService.Vote vote,
            ElectNewLeader electNewLeader,
            BuildBfsTree buildBfsTree
    ){
        this.context = context;
        this.electingNewLeader = electingNewLeader;
        this.vote = vote;
        this.electNewLeader = electNewLeader;
        this.buildBfsTree = buildBfsTree;
    }

    @Override
    public void run() {
        electNewLeader.run();
        try {
            electingNewLeader.acquire();
        } catch (InterruptedException e) {
            log.warn("interrupted while waiting on leader to be elected");
        }
        log.trace("moving onto building bfs tree");
        if(vote.isThisNodeLeader()) {
            buildBfsTree.run();
        }
    }
}
