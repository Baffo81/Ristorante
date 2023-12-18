import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Waiter {

    public static void main(String[] args) {
        final int PORT_TO_CUSTOMER = 1316,      // used for the communication with customers
                PORT_TO_CHEF = 1315;          // used for the communication with the chef

        // creates a socket to communicate with customers
        try (ServerSocket serverSocket = new ServerSocket(PORT_TO_CUSTOMER)) {

            // creates a socket to communicate with the chef
            try (Socket chefSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF)) {

                do {

                    // waits for an order by a customer
                    System.out.println("(Cameriere) Attendo clienti");
                    Socket acceptedCustomer = serverSocket.accept();

                    // creates a new thread to manage a request
                    Thread waiter = new Thread(new WaiterHandler(acceptedCustomer, chefSocket));
                    waiter.start();
                    waiter.join();
                } while (true);
            } catch (IOException exc) {
                System.out.println("(Cameriere) Impossibile comunicare con il cuoco");
                throw new RuntimeException(exc);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException exc) {
            System.out.println("(Cameriere) Impossibile comunicare con il cliente");
            throw new RuntimeException(exc);
        }
    }


    public static class WaiterHandler implements Runnable {

        protected final Socket accepted;        // identifies which client is connected
        protected final Socket chef;

        // constructor
        public WaiterHandler(Socket accepted, Socket chefSocket) {
            this.accepted = accepted;
            this.chef = chefSocket;
        }

        public void run() {
            try (BufferedReader readOrder = new BufferedReader(new InputStreamReader(accepted.getInputStream()));
                 PrintWriter sendOrder = new PrintWriter(chef.getOutputStream(), true);
                 BufferedReader readReadyOrder = new BufferedReader(new InputStreamReader(chef.getInputStream()));
                 PrintWriter sendReadyOrder = new PrintWriter(accepted.getOutputStream(), true)) {

                String order;

                do {
                    order = readOrder.readLine();

                    if (order == null || order.equalsIgnoreCase("fine")) {
                        System.out.println("Il cliente se ne è andato");
                        sendOrder.println(order);
                        break;
                    }

                    processOrder(order, sendOrder, readReadyOrder, sendReadyOrder);

                } while (true);

            } catch (IOException exc) {
                System.out.println("(Cameriere) Errore lettura/scrittura dalla socket");
                throw new RuntimeException(exc);
            } finally {
                // once customer has finished, closes the connection
                try {
                    accepted.close();
                } catch (IOException exc) {
                    System.out.println("(Cameriere) Impossibile chiudere la connessione");
                    throw new RuntimeException(exc);
                }
            }
        }

        private void processOrder(String order, PrintWriter sendOrder, BufferedReader readReadyOrder, PrintWriter sendReadyOrder) throws IOException {
            System.out.println("(Cameriere) Il cliente ordina " + order + ", mando l'ordine allo chef per prepararlo e attendo");
            sendOrder.println(order);

            order = readReadyOrder.readLine();

            if (order == null || order.equalsIgnoreCase("fine")) {
                System.out.println("Il cliente se ne è andato");
                sendOrder.println(order);
                Thread.currentThread().interrupt();
            } else {
                System.out.println("(Cameriere) " + order + " pronto, lo porto al cliente");
                sendReadyOrder.println(order);
            }
        }
    }
}