import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Cameriere extends Thread
{
    public static void main(String [] args)
    {
        final int PORT_TO_CLIENT = 1313;                                //number of the port used for the connection to the client
        final int PORT_TO_CHEF = 1314;                                  //number of the port used for the connection to the chef
        final Semaphore availableSeats = new Semaphore(100);    //number of available seats for customers

        //creates a socket with the specified port's number to connect with the chef
        try (Socket socketToChef = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF))
        {
            while (true)
            {
                //creates a socket with the specified port's number to communicate with the customer
                try (ServerSocket socketToClient = new ServerSocket(PORT_TO_CLIENT))
                {
                    while (true)
                    {
                        //waits for a customer
                        System.out.println("(Cameriere " + Thread.currentThread().threadId()+ ")" + " In attesa di clienti");

                        //arrives a customer
                        Socket acceptedClient = socketToClient.accept();
                        System.out.println("(Cameriere " + Thread.currentThread().threadId() + ")" + " Prendo la prenotazione del cliente");

                        //objects for reading and writing through the socket
                        BufferedReader readerToClient = new BufferedReader(new InputStreamReader(acceptedClient.getInputStream()));
                        PrintWriter writerToClient = new PrintWriter(acceptedClient.getOutputStream(), true);

                        //gets how many seats the customer requires
                        int requiredSeats = Integer.parseInt(readerToClient.readLine());

                        //if there are enough available seats, the customer can enter the restaurant and take them
                        if (availableSeats.tryAcquire(requiredSeats))
                        {
                            //upgrades number of available seats
                            System.out.println("(Cameriere" + Thread.currentThread().threadId() + ")" + "Il cliente prende posto");
                            writerToClient.println(true);
                            System.out.println("(Cameriere" + Thread.currentThread().threadId() + ")" + "Numero dei posti aggiornati:" + availableSeats.availablePermits());

                            //the customer exits the restaurant and its seats are now free
                            availableSeats.release(requiredSeats);
                        }
                        else
                        {
                            System.out.println("(Cameriere " + Thread.currentThread().threadId() + ")" + " Non ci sono abbastanza posti");
                            writerToClient.println(false);
                        }
                    }
                }
                catch (IOException exc)
                {
                    System.out.println("(Server) Errore creazione socket o impossibile connettersi al cliente");
                }
            }
        }
        catch (IOException exc)
        {
            System.out.println("(Server) Errore durante la comunicazione con lo chef");
        }
    }
}