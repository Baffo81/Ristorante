import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Chef {
    public static void main(String[] args) {
        final int PORT = 1315;              // used for communication with waiters
        Socket acceptedEmployee;            // specifies which waiter is communicating with the chef
        BufferedReader takeOrder;           // used to receive request of preparing an order by waiters
        String message;                     // combination of customer's order and table number
        String order;                       // requested order by a waiter
        int tableNumber;                    // number of customer's table

        // writes the menù
        writeMenu();

        // creates a server socket with the specified port to communicate with waiters
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // keeps cooking clients' orders and sending them to waiters
            while (true) {
                // waits for an order request by a waiter
                System.out.println("(Cuoco) Attendo un ordine");
                acceptedEmployee = serverSocket.accept();

                // reads an order and prepares it
                takeOrder = new BufferedReader(new InputStreamReader(acceptedEmployee.getInputStream()));
                message = takeOrder.readLine();
                String [] parts = message.split("\\|");
                order = parts[0];
                tableNumber = Integer.parseInt(parts[1]);
                acceptedEmployee.close();
                System.out.println("(Cuoco) Preparo l'ordine");
                prepareOrder(order, tableNumber);
            }
        }
        catch (IOException exc) {
            System.out.println("(Server) Errore creazione socket o impossibile connettersi al cameriere");
        }
    }

    public static void writeMenu() {
        // tries to open the file in read mode
        try (FileWriter menuWriter = new FileWriter("menu.txt")) {
            String order,                                       // order's name
                   status;                                      // "S" if the chef wants to add another order into the menu and "N" otherwise
            float price;                                        // order's price
            Scanner scanner = new Scanner(System.in);           // object to read from the stdin
            PrintWriter writer = new PrintWriter(menuWriter);   // object to write into the file

            // the chef writes the menu
            do {
                // reads order's name
                do {
                    System.out.println("Quale ordine desideri scrivere nel menù?");
                    order = scanner.nextLine();
                }
                while(order.isEmpty());

                // reads order's price
                do {
                    System.out.println("Inserisci il prezzo dell'ordine");
                    price = Float.parseFloat(scanner.next());
                }
                while (price < 0.50f);

                // writes order's name and order's price into the file separated by a line
                writer.println(order);
                writer.println(price);

                // checks if the chef wants to add another order into the menù
                System.out.println("Desideri inserire un altro ordine nel menù? (S/N)");
                status = scanner.next();
                scanner.nextLine();
            }
            while (status.equalsIgnoreCase("s"));
        }
        catch (IOException exc) {
            System.out.println("(Cuoco) Impossibile connettersi al file");
        }
    }

    // allows the chef to store prepared orders
    public static void prepareOrder(String order, int tableNumber) {

        // opens the file and writes an order and its table number
        try (FileWriter orderWriter = new FileWriter("orders.txt")) {
            PrintWriter writer = new PrintWriter(orderWriter);
            writer.println(order);
            writer.println(tableNumber);
        }
        catch (IOException exc) {
            System.out.println("(Cuoco) Impossibile connettersi al file");
        }

    }
}