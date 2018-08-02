package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

@Component
@Slf4j
public class DoStartFifoConsumer implements Runnable{
    private final BlockingQueue<Runnable> fifoWork;

    @Autowired
    public DoStartFifoConsumer(
            @Qualifier("Node/FifoConfig/fifoWork")
            BlockingQueue<Runnable> fifoWork
    ){
        this.fifoWork = fifoWork;
    }

    @Override
    public void run() {
        while(true) {
            try {
                fifoWork.take().run();
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }
}
