package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RootService {
    public void leaderElection(LeaderElectionMessage message) throws InterruptedException {
        log.warn("processing leader election message");
        //TODO implement
        Thread.sleep(1000); // simulated delay TODO: remove
    }
}
