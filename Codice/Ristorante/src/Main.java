import java.util.ArrayList;

public class Main
{
    public static void main(String [] args)
    {
        ArrayList<String> menu = Menu.getMenuObject().getMenu();
        showMenu(menu);
    }

    public static void showMenu(ArrayList<String> menu)
    {
        for (String s : menu)
            System.out.println(s);
    }
}
