package Node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Properties;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        /*
        NodeConfigurator nodeConfigurator = new NodeConfigurator();
        ThisNodeInfo thisNodeInfo = nodeConfigurator.getThisNodeInfo();
        Properties properties = new Properties();
        properties.setProperty("server.port", String(thisNodeInfo.getPort()));

        new SpringApplicationBuilder()
                .properties(properties)
                .sources(Application.class)
                .run(args);
                */
        SpringApplication.run(Application.class, args);
    }
}