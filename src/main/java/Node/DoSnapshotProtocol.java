package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Component
@Slf4j
public class DoSnapshotProtocol implements Runnable {
    private Semaphore connectingSynchronizer;

    @Autowired
    public DoSnapshotProtocol(
            @Qualifier("Node/MapConfig/connectingSynchronizer")
            Semaphore connectingSynchronizer
    ){
        this.connectingSynchronizer = connectingSynchronizer;
    }

    @Override
    public void run(){
        try {
            connectingSynchronizer.acquire();
            //TODO do the snapshot stuff
        }catch (java.lang.InterruptedException e){
            //ignore
        }
    }
}
