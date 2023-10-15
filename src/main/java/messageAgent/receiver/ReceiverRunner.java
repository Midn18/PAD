package messageAgent.receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import messageAgent.utilities.IOperation;
import messageAgent.utilities.ReceiverMessageReaderThread;
import messageAgent.utilities.TransportService;

public class ReceiverRunner {

    public static void main(String[] args) throws IOException {
        Socket socket;
        String command;
        String name;
        String message;

        BufferedReader buffRead = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Receiver started:");
        socket = new Socket("localhost", 1488);
        System.out.println("Enter your name");
        name = buffRead.readLine();
        IOperation receiver = new TransportService(socket);
        System.out.println("Input \"connect\" command to be connected to broker");
        command = buffRead.readLine();
        while (true) {
            if (command.equals("connect")) {
                message = command + " " + name + "\n";
                receiver.writeAsync(message);
                System.out.println("Connected to broker");
                break;
            } else {System.out.println("No connection");}
        }

        Runnable r = new ReceiverMessageReaderThread(receiver);
        new Thread(r).start();
        System.out.println("Input \"disconnect\" command to be disconnected from broker");
        while (true) {
            command = buffRead.readLine();
            if (command.equals("disconnect")) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socket = new Socket("localhost", 1488);
                receiver = new TransportService(socket);
                receiver.writeAsync(command + " " + name + "\n");
                break;
            }
        }
    }
}
