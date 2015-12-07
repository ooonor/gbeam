
/**
 * Write a description of class utils here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class utils
{
    // instance variables - replace the example below with your own
    private int x;

    /**
     * Constructor for objects of class utils
     */
    public utils()
    {
        // initialise instance variables
        x = 0;
    }
    public static String[] split_dir_title(String filename) {
        // backward from end of string, find the first dot / or \ or :
        String directory_name = "";
        String[] split_names = new String[2];
        int last_colon = filename.lastIndexOf(":");
        int last_slash = filename.lastIndexOf("/");
        int last_backslash = filename.lastIndexOf("\\");
        int dir_len = Math.max(last_colon, last_slash);
        dir_len = Math.max(dir_len, last_backslash);
        if(dir_len < 0) {
            split_names[0] = "";
            split_names[1] = filename;
            return (split_names);
        }
        else {
            split_names[0] = filename.substring(0, dir_len+1);
            split_names[1] = filename.substring(dir_len+1);
            return (split_names);
        }
    }

    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public int sampleMethod(int y)
    {
        // put your code here
        return x + y;
    }
}
