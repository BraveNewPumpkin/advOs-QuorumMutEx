package Node;

import java.util.Arrays;
import java.util.List;

public class MapMessage extends SimpleTargetableMessage {
    public MapMessage() {
    }

    public MapMessage(int sourceUID, int target) {
        super(sourceUID, target);
    }
}

