package messageAgent.receiver;

import java.net.Socket;
import lombok.Getter;

@Getter
public class ReceiverEntity {

    private final Socket socket;
    private final String name;
    private boolean disconnected;

    public ReceiverEntity(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
        disconnected = false;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }
}
