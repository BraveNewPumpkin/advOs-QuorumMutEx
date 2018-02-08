package Node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Properties;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        NodeConfigurator nodeConfigurator = new NodeConfigurator();
        ThisNodeInfo thisNodeInfo = nodeConfigurator.getThisNodeInfo();
        Properties properties = new Properties();
        String portNumber = String.valueOf(thisNodeInfo.getPort());
        properties.setProperty("server.port", portNumber);

        new SpringApplicationBuilder()
                .properties(properties)
                .sources(Application.class)
                .run(args);
        //SpringApplication.run(Application.class, args);
    }
}