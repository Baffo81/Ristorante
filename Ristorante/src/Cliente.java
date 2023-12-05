import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Cliente
{
    public static void main(String [] args)
    {
        final int PORT = 1313;              //port's number used for the connection to the waiter
        int requiredSeats;                  //number of required seats
        Object lock = new Object();
        boolean answer;                     //true if there are available seats and false otherwise
        String order;                       //requested order by the customer

        System.out.println("(Cliente) Attendo la disponibilità di posti");

        //tries to create a socket with specified server's address and port's number to communicate with the waiter
        try (Socket socket = new Socket(InetAddress.getLocalHost(), PORT))
        {
            //objects for reading and writing through the socket
            BufferedReader checkSeats = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            //says how many seats he needs
            requiredSeats = getRequiredSeats();
            System.out.println("(Cliente) Mi servono " + requiredSeats + " posti");

            //synchronizes the request of available seats
            synchronized (lock)
            {
                //gets waiter response
                writer.println(requiredSeats);
                answer = Boolean.parseBoolean(checkSeats.readLine());
            }

            //if there are available seats, the customer takes them
            if (answer)
            {
                //gets the menù
                System.out.println("(Cliente) Prendo posto e scannerizzo il menù");

                //keeps ordering and eating
                while (true)
                {
                    System.out.println("(Cliente) Effettuo un'ordinazione");
                    order = getOrder();
                    writer.println(order);
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
            exc.printStackTrace();
        }
    }

    //allows customer to say how many seats he needs
    public static int getRequiredSeats()
    {
        //scanner object to read the input
        Scanner scanner = new Scanner(System.in);

        //checks if the customer enters an integer
        while (!scanner.hasNextInt())
        {
            System.out.println("Benvenuto, di quanti posti hai bisogno?");
            scanner.next();
        }

        //gets customer's input, close the scanner and returns the input
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
            System.out.println("L'ordine non è disponibile, ne scelga un altro");
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
        try (FileReader fileReader = new FileReader("src/menu.txt"))
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
            exc.printStackTrace();
            return false;
        }
    }

    //simulates menu's scanning by the customer and shows it
    public static void getMenu()
    {
        ArrayList<String> menu = new ArrayList<>();

        //tries to open the file in read mode
        try (FileReader fileReader = new FileReader("src/menu.txt"))
        {
            //reads each menu order and prints them on the screen
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String menuOrder;
            while ((menuOrder = bufferedReader.readLine()) != null)
                menu.add(menuOrder);

            //close the connection to the file
            bufferedReader.close();
        }
        catch (Exception exc)
        {
            System.out.println("Errore connessione al file");
            exc.printStackTrace();
        }

        System.out.println("Questo è il menù");
        for (String s : menu)
            System.out.println(s);
    }
}
