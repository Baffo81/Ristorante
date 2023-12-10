import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Waiter {
    static final int PORT_TO_CUSTOMER = 1316,
                     PORT_TO_CHEF = 1315;

    public static void main(String[] args) {
        Waiter waiter = new Waiter();
        waiter.waiter();
    }

    public void waiter() {

        // creates a socket to communicate with customers
        try (ServerSocket serverSocket = new ServerSocket(PORT_TO_CUSTOMER)) {

            // creates a socket to communicate with the chef
            try (Socket chefSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF)) {

                while (true) {

                    // waits for an order by a customer
                    System.out.println("(Cameriere) Attendo l'ordine del cliente");
                    Socket acceptedCustomer = serverSocket.accept();

                    // creates a new thread to manage a request
                    new Thread(() -> {
                        try {
                            // used to take an order from a customer and to send it to the chef
                            BufferedReader readOrderFromCustomer = new BufferedReader(new InputStreamReader(acceptedCustomer.getInputStream()));
                            PrintWriter sendOrderToChef = new PrintWriter(chefSocket.getOutputStream());

                            // used to take a ready order from the chef and to send it back to the customer who ordered it
                            BufferedReader readReadyOrderFromChef = new BufferedReader(new InputStreamReader(chefSocket.getInputStream()));
                            PrintWriter sendReadyOrderToCustomer = new PrintWriter(acceptedCustomer.getOutputStream());

                            // gets a customer's order, sends it to the chef to prepare it, gets it ready from the chef and sends it back to the customer
                            String order;
                            order = readOrderFromCustomer.readLine();
                            System.out.println("(Cameriere) Il cliente ordina " + order + ", mando l'ordine allo chef per prepararlo e attendo");
                            sendOrderToChef.println(order);
                            order = readReadyOrderFromChef.readLine();
                            sendReadyOrderToCustomer.println(order);
                        } catch (IOException exc) {
                            System.out.println("(Cameriere) Impossibile gestire l'ordine del cliente");
                            throw new RuntimeException(exc);
                        } finally {
                            try {
                                acceptedCustomer.close();
                            } catch (IOException exc) {
                                System.out.println("(Cameriere) Impossibile chiudere la comunicazione con il cliente");
                            }
                        }
                    }).start();

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
}
