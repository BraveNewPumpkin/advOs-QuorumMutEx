package Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

@SpringBootApplication
@Slf4j
public class Application {
    public static void main(String[] args) {
        log.trace("--------before running application");
        ApplicationContext context = SpringApplication.run(Application.class, args);
        int numberOfNodes;
        log.trace("--------before running application");
        ApplicationContext context = SpringApplication.run(Application.class, args);
        Resource resource = context.getResource("file:resources/config.txt");
        try{
            InputStream is = resource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            ArrayList<String> list = new ArrayList<String>();
            HashMap<Integer,ArrayList<Integer>> neighbors =new HashMap<Integer,ArrayList<Integer>>();
            String line;
            int count=-1;
            line=br.readLine();
            numberOfNodes=Integer.parseInt(br.readLine());
            int[] node=new int[numberOfNodes];
            String[] hostNames =new String[numberOfNodes];
            int[] port=new int[numberOfNodes];
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                list.add(line);
                String words[]= line.split(" +");
               //System.out.println(words.length);
//                    for(String a:words){
//                        System.out.println(a);
//                    }
                if(words.length==1){
                        count++;
                        continue;
                }
               if(count<numberOfNodes)
                {
                      node[count]=Integer.parseInt(words[1]);
                      hostNames[count]=words[2];
                      port[count]=Integer.parseInt(words[3]);
                      count++;
                }

                if(count>numberOfNodes && count<=numberOfNodes*2)
                {
                    ArrayList<Integer> nbr=new ArrayList<Integer>();
                    for(int i=2;i<=words.length-1;i++){
                        nbr.add(Integer.parseInt(words[i]));
                    }
                    neighbors.put(Integer.parseInt(words[1]),nbr);
                }

                }
            }
            System.out.println("Neighbors are");
            for (Integer nodes : neighbors.keySet())
            {
                ArrayList<Integer> arr=neighbors.get(nodes);
                System.out.println(arr);
            }
            System.out.println("Nodes are");
            for(int a:node)
            {
                System.out.println(a);
            }
            System.out.println("Hostnames are");
            for(String a:hostNames)
            {
                System.out.println(a);
            }
            System.out.println("Ports are");
            for(int a:port)
            {
                System.out.println(a);
            }



            br.close();

        }catch(IOException e){
            e.printStackTrace();
        }
        StartLeaderElectionAndBfsTree startLeaderElectionAndBfsTree = (StartLeaderElectionAndBfsTree)context.getBean(StartLeaderElectionAndBfsTree.class);
        Thread thread = new Thread(startLeaderElectionAndBfsTree);
        log.trace("--------before running sendLeaderElection thread");
        thread.start();
    }
}