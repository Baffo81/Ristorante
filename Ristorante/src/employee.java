import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class employee extends Thread {
    final int PORT_TO_CUSTOMER = 1314;   // used for the connection to customers and to get their requests
    final int PORT_TO_CHEF     = 1315;   // used for the connection to the chef and to give it orders requests
    final static int EMPLOYEE_NUMBER = 2;
    public void run() {

            try (ServerSocket employeeSocket = new ServerSocket(PORT_TO_CUSTOMER)) {

                System.out.println("(Employee) Attendo un ordine da un cliente");
                Socket customer = employeeSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(customer.getInputStream()));
                //Recupero l'ordine dal cliente
                String orderWithTable = reader.readLine();
                System.out.println("(Employee) Ricevuto ordine: " + orderWithTable);
                //Chiudo la connessione
                customer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

    }


    public static void main(String[] args){
        //for( int i = 0; i < EMPLOYEE_NUMBER; i++){
            employee Employee = new employee();
            Employee.start();
        //}
    }
}