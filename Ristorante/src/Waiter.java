import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Waiter {
    public static void main(String[] args) {
        Waiter waiter = new Waiter();
        waiter.waiter();
    }

    public void waiter() {


        final int PORT_TO_CUSTOMER = 1316,      // used for the communication with customers
                  PORT_TO_CHEF = 1315;          // used for the communication with the chef

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
                            readOrder(readOrderFromCustomer, sendOrderToChef, readReadyOrderFromChef, sendReadyOrderToCustomer);

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

    public static void readOrder(BufferedReader readOrderFromCustomer, PrintWriter sendOrderToChef, BufferedReader readReadyOrderFromChef, PrintWriter sendReadyOrderToCustomer) throws IOException {
        String order;
        order = readOrderFromCustomer.readLine();
        System.out.println("(Cameriere) Il cliente ordina " + order + ", mando l'ordine allo chef per prepararlo e attendo");
        sendOrderToChef.println(order);
        sendOrderToChef.flush();
        order = readReadyOrderFromChef.readLine();

        // take order form Chef and gives it to Costumer
        sendOrder(sendReadyOrderToCustomer, order);
    }

    public static void sendOrder(PrintWriter sendReadyOrderToCustomer, String order){
        sendReadyOrderToCustomer.println(order);
        sendReadyOrderToCustomer.flush();
    }
}
