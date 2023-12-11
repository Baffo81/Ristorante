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
                    new Thread(() -> handleCustomer(acceptedCustomer, chefSocket)).start();
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
        System.out.println("(Cameriere) i piatti:" + order + "sono pronti");
        sendReadyOrderToCustomer.println(order);
        sendReadyOrderToCustomer.flush();
    }



    private void handleCustomer(Socket customerSocket, Socket chefSocket) {
        try (
                BufferedReader readOrderFromCustomer = new BufferedReader(new InputStreamReader(customerSocket.getInputStream()));
                PrintWriter sendOrderToChef = new PrintWriter(chefSocket.getOutputStream());
                BufferedReader readReadyOrderFromChef = new BufferedReader(new InputStreamReader(chefSocket.getInputStream()));
                PrintWriter sendReadyOrderToCustomer = new PrintWriter(customerSocket.getOutputStream(), true);
        ) {
            String order = readOrderFromCustomer.readLine();
            System.out.println("(Cameriere) Il cliente ordina " + order + ", mando l'ordine allo chef per prepararlo e attendo");

            // Invia l'ordine allo chef
            sendOrderToChef.println(order);
            sendOrderToChef.flush();

            // Attendi che lo chef prepari l'ordine
            String readyOrder = readReadyOrderFromChef.readLine();

            // Invia l'ordine pronto al cliente
            System.out.println("(Cameriere) I piatti sono pronti. Li invio al cliente.");
            sendReadyOrderToCustomer.println(readyOrder);
            sendReadyOrderToCustomer.flush();

        } catch (IOException exc) {
            System.out.println("(Cameriere) Impossibile gestire l'ordine del cliente");
            throw new RuntimeException(exc);
        } finally {
            try {
                customerSocket.close();
            } catch (IOException exc) {
                System.out.println("(Cameriere) Impossibile chiudere la comunicazione con il cliente");
            }
        }
    }
}
