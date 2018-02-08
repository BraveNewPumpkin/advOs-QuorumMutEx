package Node;

public class NodeInfo {
    private final int uid;
    private final String hostName;
    private final int port;

    public NodeInfo(int uid, String hostName, int port){
        this.uid = uid;
        this.hostName = hostName;
        this.port = port;
    }

    public int getUid() {
        return uid;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }


}
