package Node;

import java.util.Iterator;
import java.util.List;

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
}
