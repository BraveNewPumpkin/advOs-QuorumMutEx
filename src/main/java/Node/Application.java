package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
@Slf4j
public class Application {
    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(Application.class, args);
        //ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        RootController rootController = context.getBean(RootController.class);
        try{
            Thread.sleep(30000);
        }catch(InterruptedException e){
            log.warn();
        }
        rootController.sendLeaderElection();
    }
}