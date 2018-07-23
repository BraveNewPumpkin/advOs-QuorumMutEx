package Node;

import java.util.Arrays;
import java.util.List;

public class RequestMessage extends NodeMessage {
    public RequestMessage() {
    }

    public RequestMessage(int sourceUID, int sourceTimestamp, int criticalSectionNumber) {
        super(sourceUID, sourceTimestamp, criticalSectionNumber);
    }

    @Override
    public String toString() {
        return "RequestMessage{} " + super.toString();
    }
}

