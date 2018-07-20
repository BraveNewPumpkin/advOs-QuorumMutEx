package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CsRequester {
    public final CsRequesterInfo csRequesterInfo;
    public final Random random;
    private final QuorumMutExService quorumMutExService;

    @Autowired
    public CsRequester(
        QuorumMutExService quorumMutExService,
        @Qualifier("Node/NodeConfigurator/csRequester")
        CsRequesterInfo csRequesterInfo
    ) {
        this.quorumMutExService = quorumMutExService;
        this.csRequesterInfo = csRequesterInfo;

        random = new Random();
    }

    public void startRequesting(){
            while (true) {
                waitRandomInterRequestDelay();
                double csExecutionDelay = getRandomCsExecutionTime();
                long roundedCsExecutionDelay = Math.round(csExecutionDelay);
                log.trace("csExecutionDelay: before rounding: {}. sleeping for {} milliseconds", csExecutionDelay, roundedCsExecutionDelay);
                quorumMutExService.cs_enter();
                log.info("running critical section");
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
