// BlueJ Project: lesson4/book1
// Video: Working with the Book Text

import java.util.Scanner;
import java.io.File;

public class Book
{
    private String bookText;
    private String firstLine;
    private int lmn;

    public Book(String fileName)
    {
        readBook(fileName);
    }
    /**
     * Returns the number of elements in the array
     * @return the number of elements in the array
     */
    public int getLMN()
    {
        return lmn;
    }

    /**
     * Calculates the number of characters (as in letters and
     * symbols, not people) in the book.
     * @return the number of letters and symbols in the book.
     */
    public int getNumCharacters()
    {
        // TODO: Complete this method
        // You will want to use the fact sheet linked in the instructor comments.
        // Which string method do you want to use?
        return(0);
    }
    public String getFirstLine()
    {
        return firstLine;
    }

    /**
     * A method to help read the book out of the file.
     * You don't have to read this unless you want to.
     * the "try" and "catch" are java's way of dealing with
     * runtime errors or "exceptions".
     */
    public void readBook(String fileName)
    {
        bookText = "";
        firstLine = "";
        try
        {
            Scanner file = new Scanner(new File(fileName));
            if (file.hasNextLine())
            {
                firstLine = file.nextLine();
            }
            if (file.hasNextLine())
            {
                lmn = file.nextInt();
                System.out.print(" lmn = ");
                System.out.println(lmn);
            }
            while (file.hasNextLine())
            {
                String line = file.nextLine();
                bookText += line + "\n";
            }
            file.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
