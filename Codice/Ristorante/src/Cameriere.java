import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Cameriere
{
    public static void main(String [] args)
    {
        final int PORT_TO_CLIENT = 1313;        //port's number used for the connection to the client
        final int PORT_TO_CHEF   = 1314;        //port's number used for the connection to the chef
        final int MAX_WAITERS    = 5;           //number of restaurant's waiters
        int availableWaiters = MAX_WAITERS,     //number of available waiters
            availableSeats = randomNumber(),    //number of restaurant's available places
            requiredSeats;                      //number of seats required by the customer
        Menu menu;

        try
        {
            try(Socket socketToChef = new Socket(InetAddress.getLocalHost(), PORT_TO_CHEF))
            {
                //object to read chef's information
                ObjectInputStream readerToChef = new ObjectInputStream(socketToChef.getInputStream());

                //waiter waits for the menu
                menu = (Menu)readerToChef.readObject();
                System.out.println("(Cameriere) Prendo il menù dallo chef");
                menu.showMenu();

                //tries to create a socket with specified port's number to communicate with the customer
                try(ServerSocket socketToClient = new ServerSocket(PORT_TO_CLIENT))
                {
                    //waiter waits for a customer
                    System.out.println("(Cameriere) In attesa di clienti");

                    //arrives a customer
                    Socket acceptedClient = socketToClient.accept();
                    System.out.println("(Cameriere) Prendo la prenotazione del cliente");

                    //objects for reading and writing over the socket
                    BufferedReader readerToClient = new BufferedReader(new InputStreamReader(acceptedClient.getInputStream()));
                    PrintWriter    writerToClient = new PrintWriter(acceptedClient.getOutputStream(), true);

                    //gets how many seats has the customer required
                    System.out.println("(Cameriere) Abbiamo " + availableSeats + " posti disponibili");
                    requiredSeats = Integer.parseInt(readerToClient.readLine());

                    //if there are enough available seats, the customer can enter the restaurant and take them
                    if (requiredSeats <= availableSeats)
                    {
                        //there is one available waiter in less
                        availableWaiters--;

                        //client's required seats are now occupied
                        availableSeats -= requiredSeats;
                        System.out.println("(Cameriere) Il cliente prende posto");
                        writerToClient.println(true);
                        System.out.println("(Cameriere) Abbiamo " + availableSeats + " posti disponibili");

                        //the waiter sends menu to the client
                        System.out.println("(Cameriere) Fornisco il menù al cliente");
                        writerToClient.println(menu);

                        //the client exits the restaurant and its seats are now free
                        acceptedClient.close();
                        availableWaiters += requiredSeats;
                    }
                    else
                    {
                        System.out.println("(Cameriere) Non ci sono abbastanza posti");
                        writerToClient.println(false);
                        acceptedClient.close();
                    }
                }
                catch (IOException exc)
                {
                    System.out.println("(Server) Errore creazione socket o impossibile connettersi al cliente");
                    exc.printStackTrace();
                }
            }
            catch (IOException exc)
            {
                System.out.println("(Server) Errore creazione socket o impossibile connettersi allo chef");
                exc.printStackTrace();
            }
        }
        catch (Exception exc)
        {
            System.out.println("(Server) Errore interno");
            exc.printStackTrace();
        }
    }

    //generates a random number that indicates how many seats are required by the customer
    public static int randomNumber()
    {
        Random random = new Random();
        return random.nextInt(100) + 1;
    }
}
