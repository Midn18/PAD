package messageAgent.utilities;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReceiverMessageReaderThread implements Runnable {

    private final IOperation transport;

    public void run() {
        String messageFromServer;
        while (!(messageFromServer = transport.readAsync()).equals("disconnect")) {
            System.out.println(messageFromServer);
        }
        System.out.println("Disconnected from broker");
    }
}
