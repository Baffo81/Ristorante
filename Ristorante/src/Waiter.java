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

                        // gets an order by the customer and sends it to the chef to prepare it
                        try {
                            getOrder(acceptedCustomer, chefSocket);
                        } catch (IOException exc) {
                            System.out.println("(Cameriere) Impossibile prendere l'ordine dal cliente o inviarlo al cuoco");
                            throw new RuntimeException(exc);
                        }

                        // gets the order by the chef once its ready and sends it back to the customer who ordered it
                        try {
                            giveOrder(chefSocket, acceptedCustomer);
                        } catch (IOException exc) {
                            System.out.println("(Cameriere) Impossibile prendere l'ordine dal cuoco o inviarlo al cliente");
                            throw new RuntimeException(exc);
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

    // gets an order by the customer and sends it to the chef to prepare it
    public static void getOrder(Socket acceptedCustomer, Socket chefSocket) throws IOException {
        BufferedReader readOrder = new BufferedReader(new InputStreamReader(acceptedCustomer.getInputStream()));
        PrintWriter sendOrder = new PrintWriter(chefSocket.getOutputStream());
        String order = readOrder.readLine();
        System.out.println("(Cameriere) Il cliente ordina " + order + ", mando l'ordine allo chef per prepararlo e attendo");
        sendOrder.println(order);
        sendOrder.flush();
    }

    // gets the order by the chef once its ready and sends it back to the customer who ordered it
    public static void giveOrder(Socket chefSocket, Socket acceptedCustomer) throws IOException {
        BufferedReader readOrder = new BufferedReader(new InputStreamReader(chefSocket.getInputStream()));
        PrintWriter sendOrder = new PrintWriter(acceptedCustomer.getOutputStream());
        String order = readOrder.readLine();
        System.out.println("(Cameriere) " + order + " pronto, lo porto al cliente");
        sendOrder.println(order);
        sendOrder.flush();
    }
}
