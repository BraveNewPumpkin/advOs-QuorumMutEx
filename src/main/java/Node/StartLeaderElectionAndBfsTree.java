package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Slf4j
public class StartLeaderElectionAndBfsTree implements Runnable{
    @Autowired(required = true) private LeaderElectionService.Vote vote;

    private ApplicationContext context;

    public StartLeaderElectionAndBfsTree(ApplicationContext context){
        this.context = context;
    }

    @Override
    public void run() {
        Runnable electNewLeader = new ElectNewLeader(context);
        electNewLeader.run();
        //TODO protect with semaphore: electingNewLeader or something
        electingNewLeader.wait();
        if(vote.isThisNodeLeader()) {
            Runnable buildBfsTree = new BuildBfsTree(context);
            buildBfsTree.run();
        }
    }
}
