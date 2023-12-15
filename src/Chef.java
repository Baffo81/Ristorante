import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Chef {
    public static void main(String[] args) {
        final int PORT = 1315;              // used for communication with waiters
        Socket acceptedOrder;               // used to accept an order
        String order;                       // requested order by a waiter

        // writes the menù
        writeMenu();

        // creates a server socket with the specified port to communicate with waiters
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            // keeps cooking clients' orders and sending them to waiters
            while (true) {

                // waits for an order request by a waiter
                System.out.println("(Cuoco) Attendo ordini");
                acceptedOrder = serverSocket.accept();

                // creates a new thread to manage a request
                Thread chef = new Thread( new chefHandler(acceptedOrder));
                chef.start();
            }
        }
        catch (IOException exc) {
            System.out.println("(Cuoco) Impossibile comunicare con il cameriere");
            throw new RuntimeException(exc);
        }
    }

    public static void writeMenu() {
        // tries to open the file in read mode
        try (FileWriter menuWriter = new FileWriter("menu.txt")) {
            String order;                                       // order's name
            float price;                                        // order's price
            Scanner scanner = new Scanner(System.in);           // object to read from the stdin
            PrintWriter writer = new PrintWriter(menuWriter, true);   // object to write into the file

            // the chef writes the menu
            do {

                // reads order's name
                do {
                    System.out.println("Scrivi l'ordine da aggiungere al menù o digita 'fine' per confermare il menù");
                    order = scanner.nextLine();
                }
                while(order.isEmpty());

                // if the customer stops eating
                if (order.equalsIgnoreCase("fine"))
                    break;

                // reads order's price
                do {
                    System.out.println("Inserisci il prezzo dell'ordine");
                    price = Float.parseFloat(scanner.nextLine());
                }
                while (price < 0.50f);

                // writes order's name and order's price into the file separated by a line
                writer.println(order);
                writer.println(price);
            }
            while (true);
        }
        catch (IOException exc) {
            System.out.println("(Cuoco) Errore scrittura menù");
            throw new RuntimeException(exc);
        }
    }

    // gets an order to prepare by a waiter
    public static String getOrder(Socket acceptedOrder) throws IOException {
        BufferedReader takeOrder = new BufferedReader(new InputStreamReader(acceptedOrder.getInputStream()));
        return takeOrder.readLine();
    }

    // simulates the preparation of an order by the chef
    public static void prepareOrder(String order) {
        System.out.println("(Cuoco) Preparo: " + order);
        try {
            Thread.sleep(3000);
        } catch(InterruptedException exc) {
            System.out.println("(Cuoco) Errore utilizzo sleep");
            throw new RuntimeException(exc);
        }
        System.out.println("(Cuoco) " + order + " pronto");
    }

    // sends a ready order to the waiter who has required to prepare it
    public static void giveOrder(Socket acceptedOrder, String order) throws IOException {
        PrintWriter sendOrder = new PrintWriter(acceptedOrder.getOutputStream(), true);
        System.out.println("(Cuoco) Invio " + order + " al cameriere");
        sendOrder.println(order);
        sendOrder.flush();
    }


     static class chefHandler implements Runnable{

        protected final Socket accepted;

        //contructor
         public chefHandler(Socket accepted){
             this.accepted = accepted;
         }

         public void run() {

             // gets the order to prepare by the waiter
             String order = null;
             while (true) {
                 try {
                     order = getOrder(accepted);
                     if(order.equalsIgnoreCase("fine"))
                         break;
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
                 // prepares the order
                 prepareOrder(order);
                 try {
                     // gives back the order to the waiter
                     giveOrder(accepted, order);
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }

             }
         }
     }
}
