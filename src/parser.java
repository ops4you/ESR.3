import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.stream.Stream;

public class parser {
    public static int[][] parsegraph(String file) throws FileNotFoundException{
        int[][] matrix;
        File f = new File(file);
        Scanner input = new Scanner(f);
        String token = input.nextLine();

        int n = Integer.parseInt(token); 
        
        matrix = new int[n][n];
        
        for(int i = 0 ; i<n && input.hasNext();i++ ){
            token = input.nextLine();
            matrix[i] = Stream.of(token.split(" ")).mapToInt(Integer::parseInt).toArray(); //string array to int array
        }

        printm(matrix);
        input.close();
        return matrix;
    }

    public static void printm(int[][] m){
        for (int[] is : m) {
            for (int is2 : is) {
                System.out.print(is2+" ");
            }
            System.out.println(" ");
        }
    }

    public static String[] parseips(String file) throws FileNotFoundException{
        String[] ips;
        File f = new File(file);
        Scanner input = new Scanner(f);
        String token = input.nextLine();
        
        int n = Integer.parseInt(token); 
        
        ips = new String[n];
      
        //reading all the lines that are not ips
        for(int i=0; i<n && input.hasNext() ; i++){
            input.nextLine();
        }

        // reading all ips
        for(int i=0; i<n && input.hasNext() ; i++){
            token = input.nextLine();
            ips[i] = token;
        }

        printips(ips);
        input.close();
        return ips;

    }

    public static void printips(String ips[]){
        for (String string : ips) {
            System.out.println(string);
        }
    }

    public static void main(String[] args) {
        try {
            String file = "src/network.txt";
            parsegraph(file);
            parseips(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
