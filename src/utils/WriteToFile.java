package utils;

import java.io.*;

/**
 * Created by miha.novak on 02/09/2017.
 */
public class WriteToFile {

    public static void writeDataToFile(String fileName, String data) {
        data = data.replace(" ", padRight(" ", 20));
        File log = new File(fileName);
        try {
            PrintWriter out = new PrintWriter(new FileWriter(log, true));
            BufferedWriter bufferedWriter = new BufferedWriter(out);
            bufferedWriter.write(data + "\n");
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("COULD NOT LOG!!");
            e.printStackTrace();
        }
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }
}