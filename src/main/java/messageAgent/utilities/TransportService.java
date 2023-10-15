package messageAgent.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransportService implements IOperation {

    Socket transport;

    @Override
    public String readAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> task = () -> {
            InputStream istream;
            String partlyTransData;
            StringBuilder result = new StringBuilder();
            try {
                istream = transport.getInputStream();
                BufferedReader receiveRead = new BufferedReader(new InputStreamReader(istream));
                if (!(partlyTransData = receiveRead.readLine()).isEmpty()) {result.append(partlyTransData.trim());}
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        };
        Future<String> future = executor.submit(task);
        String message = "";
        while (!future.isDone()) {
            try {
                message = future.get();
            } catch (InterruptedException | ExecutionException ie) {
                ie.printStackTrace(System.err);
            }
        }
        executor.shutdown();
        return message;
    }

    @Override
    public void writeAsync(String message) {
        Thread thread = new Thread(() -> {
            OutputStream outputStream;
            try {
                outputStream = transport.getOutputStream();
                PrintWriter pwrite = new PrintWriter(outputStream, true);
                pwrite.println(message);
                pwrite.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}
