package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
@Slf4j
public class MapConfig {
    private final ThisNodeInfo thisNodeInfo;

    @Autowired
    public MapConfig(
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo
    ) {
        this.thisNodeInfo = thisNodeInfo;
    }

    @Bean
    @Qualifier("Node/MapConfig/connectingSynchronizer")
    public Semaphore getConnectingSynchronizer() {
         Semaphore connectingSynchronizer = new Semaphore(1);
         try {
             connectingSynchronizer.acquire();
         }catch (java.lang.InterruptedException e){
             //ignore
         }
         return connectingSynchronizer;
    }
}
