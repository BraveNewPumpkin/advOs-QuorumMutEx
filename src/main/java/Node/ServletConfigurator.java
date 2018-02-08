package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfigurator {
    @Autowired
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    ThisNodeInfo thisNodeInfo;

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return (container -> {
            container.setPort(thisNodeInfo.getPort());
        });
    }
}
