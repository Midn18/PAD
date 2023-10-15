package messageAgent.broker;

import messageAgent.utilities.IOperation;

public class BrokerRunner {

    public static void main(String[] args) {
        System.out.println("Broker started:");
        IOperation broker = new BrokerService();
        String msg;
        while (true) {
            while (!(msg = broker.readAsync()).isEmpty()) {
                if (msg.equals("invalid")) {
                    System.out.println("Broker loop");
                    System.out.println("INVALID MESSAGE");
                } else {
                    System.out.println("Broker loop");
                    System.out.println("VALID MESSAGE");
                    broker.writeAsync(msg);
                }
            }
        }
    }
}
