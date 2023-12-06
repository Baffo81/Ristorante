import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Cliente
{
    public static void main(String [] args)
    {
        final int PORT_TO_RECEPTION = 1313; //port's number used for the connection to the reception
        final int PORT_TO_WAITER    = 1315; //port's number used for the connection to waiters
        int requiredSeats;                  //number of required seats
        Object lock = new Object();         //used to check that just one customer a time requires seats
        boolean answer;                     //true if there are available seats and false otherwise
        String order;                       //requested order by the customer

        //tries to create a socket with specified server's address and port's number to communicate with the waiter
        try (Socket receptionSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_RECEPTION))
        {
            //objects for reading and writing through the socket
            BufferedReader checkSeats = new BufferedReader(new InputStreamReader(receptionSocket.getInputStream()));
            PrintWriter seatsWriter = new PrintWriter(receptionSocket.getOutputStream(), true);

            //says how many seats he needs
            requiredSeats = getRequiredSeats();
            System.out.println("(Cliente) Mi servono " + requiredSeats + " posti");

            //synchronizes the request of available seats
            synchronized (lock)
            {
                //gets waiter response
                seatsWriter.println(requiredSeats);
                answer = Boolean.parseBoolean(checkSeats.readLine());
                receptionSocket.close();
            }

            //if there are available seats, the customer takes them
            if (answer)
            {
                //gets the menù
                System.out.println("(Cliente) Prendo posto e scannerizzo il menù");

                //keeps ordering and eating
                while (true)
                {
                    try(Socket waiterSocket = new Socket(InetAddress.getLocalHost(), PORT_TO_WAITER))
                    {
                        //objects for reading and writing through the socket
                        BufferedReader orderReader = new BufferedReader(new InputStreamReader(waiterSocket.getInputStream()));
                        PrintWriter orderWriter = new PrintWriter(waiterSocket.getOutputStream(), true);

                        //orders, wait for the order and eats it
                        System.out.println("(Cliente) Effettuo un'ordinazione");
                        order = getOrder();
                        System.out.println("(Cliente) Ordino " + order);
                        orderWriter.println(order);
                        order = orderReader.readLine();
                        System.out.println("(Cliente) Mangio " + order);
                    }
                }
            }

            //otherwise, he goes away
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

    //allows customer to say how many seats he needs
    public static int getRequiredSeats()
    {
        //scanner object to read the input
        Scanner scanner = new Scanner(System.in);

        //reads customer requested seats
        System.out.println("Benvenuto, di quanti posti hai bisogno?");
        int x = scanner.nextInt();
        scanner.close();
        return x;
    }

    public static String getOrder()
    {
        //scanner object to read the input
        Scanner scanner = new Scanner(System.in);

        //customer's order
        String order;

        //show the menu to the customer
        getMenu();

        //gets customer's order
        System.out.println("Scegli un ordine da effettuare");
        order = scanner.nextLine();

        //checks if customer's requested order is in the menù
        while (!checkOrder(order))
        {
            System.out.println("Ordine non disponibile, scegline un altro");
            order = scanner.nextLine();
        }

        //closes the scanner and returns the order
        scanner.close();
        return order;
    }

    //checks if customer's requested order is in the menù
    public static boolean checkOrder(String order)
    {
        //tries to open the file in read mode
        try (FileReader fileReader = new FileReader("menu.txt"))
        {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String menuOrder;
            while ((menuOrder = bufferedReader.readLine()) != null)
                if (menuOrder.equals(order))
                    return true;

            //close the connection to the file
            bufferedReader.close();
            return false;
        }
        catch (Exception exc)
        {
            System.out.println("Errore connessione al file");
            return false;
        }
    }

    //simulates menu's scanning by the customer and shows it
    public static void getMenu()
    {
        //tries to open the file in read mode
        try (FileReader fileReader = new FileReader("menu.txt"))
        {
            //reads each menu order and prints them on the screen
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String order;
            float price;
            System.out.println("Questo è il menù:");
            while ((order = bufferedReader.readLine()) != null)
            {
                price = Float.parseFloat(bufferedReader.readLine().trim());
                System.out.println("Ordine: " + order);
                System.out.println("Prezzo: " + price);
            }

            //close the connection to the file
            bufferedReader.close();
        }
        catch (Exception exc)
        {
            System.out.println("Errore scannerizzazione menù");
        }
    }
}
