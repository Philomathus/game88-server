package tv.game88.wallet.api.sse.model;

import lombok.Builder;
import lombok.Value;
import tv.game88.wallet.api.type.StreamMessageType;

import java.io.Serializable;

@Value
@Builder
public class SimpleProtocolMessage<T> implements Serializable {
    StreamMessageType messageType;
    T                 data;
}
