import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;

public class Menu implements Serializable
{
    protected ArrayList<String> menu = new ArrayList<>();

    public Menu() {}

    public ArrayList<String> getMenu()
    {
        //name of the file that contains the menu
        String fileName = "menu.txt";

        //tries to open the file in read mode
        try (FileReader fileReader = new FileReader(fileName))
        {
            //reads each menu order and prints them on the screen
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String menuOrder;
            while ((menuOrder = bufferedReader.readLine()) != null)
                menu.add(menuOrder);

            //close the connection to the file
            bufferedReader.close();

            return menu;
        }
        catch (Exception exc)
        {
            System.out.println("Errore connessione al file");
            return null;
        }
    }

    public void showMenu()
    {
        for (String s : menu)
            System.out.println(s);
    }
}
