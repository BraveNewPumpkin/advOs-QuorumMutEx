package Node;

import java.util.Arrays;
import java.util.List;

public class RequestMessage extends NodeMessage {
    private int sourceTimestamp;

    public RequestMessage() {
    }

    public RequestMessage(int sourceUID, int sourceTimestamp) {
        super(sourceUID);
        this.sourceTimestamp = sourceTimestamp;
    }

    public void setSourceTimestamp(int sourceTimestamp) {
        this.sourceTimestamp = sourceTimestamp;
    }

    public int getSourceTimestamp() {
        return sourceTimestamp;
    }

    @Override
    public String toString() {
        return "RequestMessage{" +
                "sourceTimestamp=" + sourceTimestamp +
                "} " + super.toString();
    }
}

