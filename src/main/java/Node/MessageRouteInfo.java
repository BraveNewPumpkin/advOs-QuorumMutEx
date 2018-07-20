package Node;

import java.util.function.Consumer;

public class MessageRouteInfo<T extends NodeMessage> {
    private final String destinationPath;
    private final Class<T> messageClass;
    private final Consumer<T> handlerMethod;

    public MessageRouteInfo(String destinationPath, Class<T> messageClass, Consumer<T> handlerMethod){
        this.destinationPath = destinationPath;
        this.messageClass = messageClass;
        this.handlerMethod = handlerMethod;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public Class<T> getMessageClass() {
        return messageClass;
    }

    public Consumer<T> getHandlerMethod() {
        return handlerMethod;
    }
}
