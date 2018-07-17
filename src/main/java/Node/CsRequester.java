package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CsRequester {
    public final CsRequesterInfo csRequesterInfo;
    public final Random random;

    public CsRequester(
        @Qualifier("Node/NodeConfigurator/csRequester")
        CsRequesterInfo csRequesterInfo
    ) {
        this.csRequesterInfo = csRequesterInfo;

        random = new Random();
    }

    public void startRequesting(){
        try {
            while (true) {
                double interRequestDelay = getRandomInterRequestDelay();
                long roundedInterRequestDelay = Math.round(interRequestDelay);
                log.trace("interRequestDelay: before rounding: {}. sleeping for {} milliseconds", interRequestDelay, roundedInterRequestDelay);
                TimeUnit.MILLISECONDS.sleep(roundedInterRequestDelay);
            }
        } catch (java.lang.InterruptedException e) {
            //ignore
        }
    }

    public double getRandomInterRequestDelay() {
        double lambda = getLambdaGivenMean(csRequesterInfo.getInterRequestDelay());
        return getRandomSampleFromExponentialDistribution(lambda);
    }

    public double getRandomCsExecutionTime() {
        double lambda = getLambdaGivenMean(csRequesterInfo.getCsExecutionTime());
        return getRandomSampleFromExponentialDistribution(lambda);
    }

    private double getLambdaGivenMean(double mean) {
        return 1.0 / mean;
    }

    public double getRandomSampleFromExponentialDistribution(double lambda) {
        return Math.log(1 - random.nextDouble())/(-lambda);
    }
}
