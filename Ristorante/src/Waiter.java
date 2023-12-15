import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Waiter {

    public static void main(String [] args) {
        final int PORT_TO_CUSTOMER = 1316,      // used for the communication with customers
                PORT_TO_CHEF = 1315;          // used for the communication with the chef

        // creates a socket to communicate with customers
        try (ServerSocket serverSocket = new ServerSocket(PORT_TO_CUSTOMER)) {

            // creates a socket to communicate with the chef
            try (Socket chefSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF)) {

                while (true) {

                    // waits for an order by a customer
                    System.out.println("(Cameriere) Attendo clienti");
                    Socket acceptedCustomer = serverSocket.accept();

                    // creates a new thread to manage a request
                    Thread waiter = new Thread(new waiterHandler(acceptedCustomer, chefSocket));
                    waiter.start();
                }
            } catch (IOException exc) {
                System.out.println("(Cameriere) Impossibile comunicare con il cuoco");
                throw new RuntimeException(exc);
            }
        } catch (IOException exc) {
            System.out.println("(Cameriere) Impossibile comunicare con il cliente");
            throw new RuntimeException(exc);
        }
    }

    static class waiterHandler implements Runnable {

        protected final Socket accepted;        // identifies which client is connected
        protected final Socket chef;

        // constructor
        public waiterHandler(Socket accepted, Socket chefSocket) {
            this.accepted = accepted;
            this.chef = chefSocket;
        }
        public void run () {

            // used to get a customer's order and to send it to the chef to prepare it
            try (BufferedReader readOrder = new BufferedReader(new InputStreamReader(accepted.getInputStream()));
                 PrintWriter sendOrder = new PrintWriter(chef.getOutputStream(), true);

                 // used to gets the order by the chef once it has prepared it and to give it to the customer who ordered it
                 BufferedReader readReadyOrder = new BufferedReader(new InputStreamReader(chef.getInputStream()));
                 PrintWriter sendReadyOrder = new PrintWriter(accepted.getOutputStream(), true)) {

                // customer's order
                String order;

                // gets each customer's order, sends it to the chef and gives it back to the customer
                while (!Objects.equals(order = readOrder.readLine(), "fine")) {
                    System.out.println("(Cameriere) Il cliente ordina " + order + ", mando l'ordine allo chef per prepararlo e attendo");
                    sendOrder.println(order);
                    sendOrder.flush();
                    order = readReadyOrder.readLine();
                    System.out.println("(Cameriere) " + order + " pronto, lo porto al cliente");
                    sendReadyOrder.println(order);
                    sendReadyOrder.flush();
                }
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            } finally {
                // once customer has finished, closes the connection
                try {
                    accepted.close();
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }
    }
}
