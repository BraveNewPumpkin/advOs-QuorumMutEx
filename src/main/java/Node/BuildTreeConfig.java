package Node;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
public class BuildTreeConfig {
    @Bean
    @Qualifier("Node/BuildTreeConfig/treeInfo")
    public TreeInfo getTreeInfo(){
        return new TreeInfo();
    }

    @Bean
    @Qualifier("Node/BuildTreeConfig/tree")
    public Tree<Integer> getTree(){
        return new Tree<Integer>();
    }

    @Bean
    @Qualifier("Node/MapConfig/buildingTreeSynchronizer")
    public GateLock getBuildingTreeSynchronizer() {
        GateLock buildingTreeSynchronizer = new GateLock();
        buildingTreeSynchronizer.close();
        return buildingTreeSynchronizer;
    }
}
