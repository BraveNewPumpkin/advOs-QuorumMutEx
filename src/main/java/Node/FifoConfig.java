package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@Slf4j
public class FifoConfig {
    @Autowired
    public FifoConfig(

    ){

    }

    @Bean
    @Qualifier("Node/FifoConfig/fifoWork")
    public BlockingQueue<Runnable> getFifoWork() {
        return new LinkedBlockingQueue<>();
    }
}
