package tv.game88.core.sse.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SimpleProtocolMessage<T> {
    StreamMessageType messageType;
    T data;
}
