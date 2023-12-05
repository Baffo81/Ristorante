import java.util.ArrayList;

public class Main
{
    public static void main(String [] args)
    {
        Menu menuObject = new Menu();
        ArrayList<String> menu = menuObject.getMenu();
        showMenu(menu);
    }

    public static void showMenu(ArrayList<String> menu)
    {
        for (String s : menu)
            System.out.println(s);
    }
}
