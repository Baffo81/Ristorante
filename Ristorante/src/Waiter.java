import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Waiter extends Thread {
    // ------------------------------------- ports for the communication ------------------------------
    static final int PORT_TO_CHEF     = 1315,      // used for the connection with the chef
                     PORT_TO_CUSTOMER = 1316;      // used for the connection with customers
    // ------------------------------------------------------------------------------------------------

    public static void main(String [] args) {
        Waiter waiter = new Waiter();
        waiter.start();
    }

    public void waiter(String[] args) {
        Socket acceptedCustomer;            // used to accept a customer's order
        BufferedReader takeOrder,           // used to get a customer's order
                       takeReadyOrder;      // used to get chef's ready order
        PrintWriter sendOrder,              // used to send a customer's order to the chef
                    sendReadyOrder;         // used to send to the customer the ready order
        String order;                       // requested order by a customer
        int tableNumber;                    // number of customer's table


        //waits for a customer's order

    }

    public void run() {
        System.out.println("(Impiegato " + Thread.currentThread().threadId() + ") Attendo l'ordine di un cliente");

    }

}
