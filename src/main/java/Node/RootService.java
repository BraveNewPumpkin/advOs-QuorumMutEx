package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RootService {
    public void leaderElection(LeaderElectionMessage message) throws InterruptedException {
        //TODO: convert to trace
        log.error("--------processing leader election message");
        //TODO implement
    }
}
