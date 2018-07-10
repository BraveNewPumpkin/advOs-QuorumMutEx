package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class QuorumMutExConfig {

    @Autowired
    public QuorumMutExConfig(
    ) {
    }

    @Bean
    @Qualifier("Node/QuorumMutExConfig/quorumMutExInfo")
    public QuorumMutExInfo quorumMutExInfo(){
        return new QuorumMutExInfo();
    }
}
