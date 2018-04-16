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
