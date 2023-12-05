import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class Cliente
{
    public static void main(String [] args)
    {
        final int PORT         = 1313;               //port's number used for the connection to the waiter
        int     requiredSeats  = randomNumber();     //number of required seats
        boolean answer;                              //true if there are available seats and false otherwise
        Menu menu;

        try
        {
            System.out.println("(Cliente) Attendo la disponibilità di posti");

            //tries to create a socket with specified server's address and port's number to communicate with the waiter
            try (Socket socket = new Socket(InetAddress.getLocalHost(), PORT))
            {
                //objects for reading and writing over the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                //says how many seats he needs
                System.out.println("(Cliente) Mi servono " + requiredSeats + " posti");
                writer.println(requiredSeats);

                //gets waiter response
                answer = Boolean.parseBoolean(reader.readLine());

                //if there are available seats, the customer takes them
                if (answer)
                {
                    System.out.println("(Cliente) Prendo posto e richiedo il menù");

                    try(ObjectInputStream readerObject = new ObjectInputStream(socket.getInputStream())){
                        menu = (Menu) readerObject.readObject();
                        System.out.println("(Cliente) Il menù è " );
                        menu.showMenu();
                    }
                    catch(IOException | ClassNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    System.out.println("(Cliente) Me ne vado!");
                }
            }
            catch (IOException exc)
            {
                System.out.println("(Client) Errore creazione socket o impossibile connettersi al server");
            }
        }
        catch (Exception exc)
        {
            System.out.println("(Client) Errore interno");
        }
    }

    //generates a random number that indicates how many seats are required by the customer
    public static int randomNumber()
    {
        Random random = new Random();
        return random.nextInt(10) + 1;
    }
}
