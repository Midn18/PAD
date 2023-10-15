package messageAgent.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import messageAgent.receiver.ReceiverEntity;
import messageAgent.utilities.DataParserManager;
import messageAgent.utilities.IOperation;
import messageAgent.utilities.Letter;

public class BrokerService implements IOperation {

    private ServerSocket serverSocket;
    private final List<Letter> letterList;
    private final List<ReceiverEntity> receiversList;

    public BrokerService() {
        letterList = new ArrayList<>();
        receiversList = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(1488);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String readAsync() {
        Socket connectionSocket = null;
        Consumer<ReceiverEntity> styleRec = (ReceiverEntity p) -> System.out.println(
            "Name: " + p.getName() + ", Socket: " + p.getSocket());
        Consumer<Letter> printLetterConsumer = (Letter l) -> System.out.print(
            "Name:" + l.getName() + ", Message text: " + l.getMessage());
        try {
            connectionSocket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Socket finalConnectionSocket = connectionSocket;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<String> task = () -> {
            InputStream inputStream;
            StringBuilder result = new StringBuilder();
            try {
                assert finalConnectionSocket != null;
                inputStream = finalConnectionSocket.getInputStream();
                BufferedReader receiveRead = new BufferedReader(new InputStreamReader(inputStream));
                String partlyTransData;
                while (!(partlyTransData = receiveRead.readLine()).isEmpty()) {result.append(partlyTransData.trim());}
            } catch (IOException e) {
                e.printStackTrace();
            }
            String message = result.toString();
            String answer = "valid";
            System.out.println("--Received data from buffer--");
            System.out.println(message);
            DataParserManager xml = new DataParserManager(message);
            if (message.length() >= 9 && message.substring(0, 8).equals("connect ")) {
                String name = message.substring(8, message.length());
                System.out.println("parsed receiver: " + name + " to be connected");
                receiversList.add(new ReceiverEntity(finalConnectionSocket, name));
            } else if (message.length() >= 12 && message.substring(0, 11).equals("disconnect ")) {
                String name = message.substring(11, message.length());
                System.out.println("parsed receiver: " + name + " to be disconnected");

                letterList.add(new Letter(name, "disconnect\n"));

            } else if (xml.CheckIfXml()) {
                System.out.println("--Parsed data--");
                String msg = xml.getMessage();
                System.out.println("Message from method : " + msg);
                List<String> rec = xml.getReceivers();
                System.out.println("Receivers : " + rec);
                for (String s : rec) {letterList.add(new Letter(s, msg + "\n"));}
            } else {
                answer = "invalid";
                System.out.println("Message is not valid");
                int port = finalConnectionSocket.getPort();
                for (ReceiverEntity receiverEntity : receiversList) {
                    if (receiverEntity.getSocket().getPort() == port) {
                        letterList.add(new Letter(receiverEntity.getName(), "IDIOT" + "\n"));
                        break;
                    }
                }
            }
            System.out.println("---Receiver List:---");
            receiversList.forEach(styleRec);
            System.out.println("--Letters List--");
            letterList.forEach(printLetterConsumer);
            return answer;
        };

        Future<String> future = executor.submit(task);
        String message = null;
        while (!future.isDone()) ;
        try {
            message = future.get();
        } catch (InterruptedException ie) {
            ie.printStackTrace(System.err);
        } catch (ExecutionException ee) {
            ee.printStackTrace(System.err);
        }
        executor.shutdown();
        return message;
    }

    @Override
    public void writeAsync(String message) {
        BiConsumer<ReceiverEntity, Letter> recLetterMatch = (receiverEntity, letter) -> {
            if (receiverEntity.getName().equals(letter.getName())) {
                try {
                    OutputStream ostream = receiverEntity.getSocket().getOutputStream();
                    PrintWriter pwrite = new PrintWriter(ostream, true);
                    String messageToRec = letter.getMessage();
                    pwrite.println(messageToRec);
                    pwrite.flush();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    letter.setSent(true);
                    System.out.println("--Receiver name and Letter name address MATCH--");
                    System.out.println("Receiver name: " + receiverEntity.getName());
                    System.out.println("Letter name: " + letter.getName());
                    System.out.println(
                        "Message: " + letter.getMessage() + " was transmitted to " + letter.getName() + " succesfully...");
                    if (letter.getMessage().equals("disconnect\n")) {
                        receiverEntity.setDisconnected(true);
                        receiverEntity.getSocket().close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable r = () -> {
            int[] finalI = {0};
            for (int i = 0; i < letterList.size(); i++) {
                finalI[0] = i;
                receiversList.forEach(a -> recLetterMatch.accept(a, letterList.get(finalI[0])));
            }
            letterList.removeIf(Letter::isSent);
            receiversList.removeIf(ReceiverEntity::isDisconnected);
        };
        Thread t = new Thread(r);
        t.start();
    }
}
