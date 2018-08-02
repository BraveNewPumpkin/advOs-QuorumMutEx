package Node;

public class FifoRequestId {
    private String requestId;

    public FifoRequestId(){
    }

    public FifoRequestId(String requestId){
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "FifoRequestId{" +
                "requestId='" + requestId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FifoRequestId that = (FifoRequestId) o;

        return requestId != null ? requestId.equals(that.requestId) : that.requestId == null;
    }

    @Override
    public int hashCode() {
        return requestId != null ? requestId.hashCode() : 0;
    }
}
