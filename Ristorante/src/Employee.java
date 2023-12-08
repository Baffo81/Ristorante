import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Employee extends Thread {
    final int PORT_TO_CUSTOMER = 1314;      // used for the connection with customers
    final int PORT_TO_CHEF     = 1315;      // used for the connection to the chef and to give it orders requests
    final static int EMPLOYEE_NUMBER = 2;   // number of restaurant's employees
    public void run() {
        Socket acceptedCustomer;            // used to accept a customer's order
        BufferedReader takeOrder;           // used to get a customer's order
        PrintWriter sendOrder;              // used to send a customer's order to the chef
        String message;                     // combination of customer's order and table number
        String order;                       // customer's order requested by waiter
        int tableNumber;                    // number of customer's table

        // creates a socket to get customers' orders and a socket to send them to the chef
        try (ServerSocket customerSocket = new ServerSocket(PORT_TO_CUSTOMER);
             Socket chefSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF)) {

                //waits for a customer's order
                System.out.println("(Impiegato " + Thread.currentThread().threadId() + ") Attendo l'ordine di un cliente");
                acceptedCustomer = customerSocket.accept();

                // reads an order from a customer and sends it to the chef
                takeOrder = new BufferedReader(new InputStreamReader(acceptedCustomer.getInputStream()));
                sendOrder = new PrintWriter(chefSocket.getOutputStream(), true);
                message = takeOrder.readLine();
                String [] parts = message.split("\\|");
                order = parts[0];
                tableNumber = Integer.parseInt(parts[1]);
                System.out.println("(Impiegato " + Thread.currentThread().threadId() + ") Prendo un ordine e lo mando allo chef");
                sendOrder.write(order + "|" + tableNumber);
                acceptedCustomer.close();
            }
            catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }

    public static void main(String[] args){
        //for( int i = 0; i < EMPLOYEE_NUMBER; i++){
            Employee Employee = new Employee();
            Employee.start();
        //}
    }
}