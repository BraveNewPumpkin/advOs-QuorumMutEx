package Node;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
@Slf4j
public class GHSUtil {

    public static synchronized boolean checkList(ThisNodeInfo nodeInfo, Edge edge)
    {
        List<Edge> treeList = nodeInfo.getTreeEdges();

        synchronized (treeList)
        {
            for(Iterator<Edge> itr=treeList.iterator(); itr.hasNext();)
            {
                Edge e = itr.next();

                if(e.equals(edge))
                    return true;
            }

            return false;
        }
    }

    public static synchronized boolean receivedAllMwoeSearchMessages(ThisNodeInfo node, int phaseNumber)
    {
        int neighbors = node.getNeighbors().size();
        List<MwoeSearchMessage> messages = node.getMwoeSearchBuffer();
        int count=0;
        synchronized (messages)
        {
            for(Iterator<MwoeSearchMessage> itr=messages.iterator(); itr.hasNext();)
            {
                MwoeSearchMessage m = itr.next();
                if(m.getPhaseNumber()==phaseNumber)
                    count++;
            }
        }
        System.out.println(count);
        return count==neighbors;
    }

    public static void printTreeEdgeList(List<Edge> treeEdges) {
        System.out.println("All tree edges: ");
        synchronized (treeEdges) {
            for (Iterator<Edge> itr = treeEdges.iterator(); itr.hasNext(); ) {
                Edge edge = itr.next();
                System.out.println(edge.toString());
            }
        }
    }
}
