import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Receptionist
{
    public static void main(String [] args)
    {
        final int PORT = 1313;                                          //number of the port on which communicates with customers
        final Semaphore availableSeats = new Semaphore(100);    //number of available seats for customers

        //creates a socket to connect with customers
        try(ServerSocket receptionSocket = new ServerSocket(PORT))
        {
            //keeps checking customers' booking
            while(true)
            {
                System.out.println("(Reception) In attesa di prenotazioni da clienti");

                //arrives a customer
                Socket acceptedClient = receptionSocket.accept();

                //objects for reading and writing through the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(acceptedClient.getInputStream()));
                PrintWriter writer = new PrintWriter(acceptedClient.getOutputStream(), true);

                //gets how many seats the customer requires
                int requiredSeats = Integer.parseInt(reader.readLine());

                //if there are enough available seats, the customer can enter the restaurant and take them
                if (availableSeats.tryAcquire(requiredSeats))
                {
                    //upgrades number of available seats
                    System.out.println("(Reception) Il cliente prende posto");
                    writer.println(true);
                    System.out.println("(Reception) Numero dei posti aggiornati:" + availableSeats.availablePermits());

                    //the customer exits the restaurant and its seats are now free
                    availableSeats.release(requiredSeats);
                }
                else
                {
                    System.out.println("(Reception) Non ci sono abbastanza posti");
                    writer.println(false);
                }
            }
        }
        catch (IOException exc)
        {
            System.out.println("(Reception) Errore creazione socket o impossibile connettersi al cliente");
        }
    }
}
