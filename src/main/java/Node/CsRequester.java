package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CsRequester {
    private final QuorumMutExService quorumMutExService;
    private final CsRequesterInfo csRequesterInfo;
    private final Semaphore connectingSynchronizer;
    private final ThisNodeInfo thisNodeInfo;
    private final Random random;

    @Autowired
    public CsRequester(
        QuorumMutExService quorumMutExService,
        @Qualifier("Node/NodeConfigurator/csRequester")
        CsRequesterInfo csRequesterInfo,
        @Qualifier("Node/ConnectConfig/connectingSynchronizer")
        Semaphore connectingSynchronizer,
        @Qualifier("Node/NodeConfigurator/thisNodeInfo")
        ThisNodeInfo thisNodeInfo
    ) {
        this.quorumMutExService = quorumMutExService;
        this.csRequesterInfo = csRequesterInfo;
        this.connectingSynchronizer = connectingSynchronizer;
        this.thisNodeInfo = thisNodeInfo;

        random = new Random();
    }

    public void startRequesting(){
        try {
            connectingSynchronizer.acquire();
        } catch(java.lang.InterruptedException e) {
            //ignore
        }
        for(int i = 0; i < thisNodeInfo.getNumberOfRequests(); i++) {
                waitRandomInterRequestDelay();
                double csExecutionDelay = getRandomCsExecutionTime();
                long roundedCsExecutionDelay = Math.round(csExecutionDelay);
                log.trace("csExecutionDelay: before rounding: {}. sleeping for {} milliseconds", csExecutionDelay, roundedCsExecutionDelay);
                quorumMutExService.cs_enter();
                log.info("running critical section number {}", csRequesterInfo.getCriticalSectionNumber());
                try {
                    TimeUnit.MILLISECONDS.sleep(roundedCsExecutionDelay);
                } catch (java.lang.InterruptedException e) {
                    //ignore
                }
                quorumMutExService.cs_leave();
            }
    }

    private void waitRandomInterRequestDelay() {
        double interRequestDelay = getRandomInterRequestDelay();
        long roundedInterRequestDelay = Math.round(interRequestDelay);
        log.trace("interRequestDelay: before rounding: {}. sleeping for {} milliseconds", interRequestDelay, roundedInterRequestDelay);
        try {
            TimeUnit.MILLISECONDS.sleep(roundedInterRequestDelay);
        } catch (java.lang.InterruptedException e) {
            //ignore
        }
    }

    private double getRandomInterRequestDelay() {
        double lambda = getLambdaGivenMean(csRequesterInfo.getInterRequestDelay());
        return getRandomSampleFromExponentialDistribution(lambda);
    }

    private double getRandomCsExecutionTime() {
        double lambda = getLambdaGivenMean(csRequesterInfo.getCsExecutionTime());
        return getRandomSampleFromExponentialDistribution(lambda);
    }

    private double getLambdaGivenMean(double mean) {
        return 1.0 / mean;
    }

    private double getRandomSampleFromExponentialDistribution(double lambda) {
        return Math.log(1 - random.nextDouble())/(-lambda);
    }
}
