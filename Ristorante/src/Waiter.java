import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Waiter extends Thread {
    final int PORT_TO_CHEF = 1315;          // used for the connection with the chef
    final int PORT_TO_CUSTOMER = 1316;      // used for the connection with customers
    final static int WAITERS_NUMBER = 3;    // number of restaurant's waiters
    public void run() {
        Socket acceptedCustomer;

        // creates a socket to get customers' orders and a socket to send them to the chef
        try (Socket chefSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF);
            ServerSocket customerSocket = new ServerSocket(PORT_TO_CUSTOMER)) {

            // waits for a ready order
            System.out.println("(Cameriere " + Thread.currentThread().threadId() + ") Attendo che lo chef prepari un ordine");


        }
        catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    public static void main(String [] args) {
        // creates three threads to simulate the waiters
        //for (int i = 0; i < WAITERS_NUMBER; i++) {
            Waiter waiter = new Waiter();
            waiter.start();
        //}
    }
}
