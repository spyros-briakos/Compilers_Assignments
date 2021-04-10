import java.io.IOException;
// import java.io.InputStream;
// import java.io.FileInputStream;  
// import java.io.ByteArrayInputStream;
// import java.util.Scanner;  

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println((new Calculator(System.in)).Eval());
            
            // //Used for test cases...
            // FileInputStream fis=new FileInputStream("txt/input.txt");       
            // Scanner sc=new Scanner(fis);    

            // while(sc.hasNextLine())  
            // {  
            //     InputStream temp = new ByteArrayInputStream(sc.nextLine().getBytes());
            //     System.out.println((new Calculator(temp)).Eval());    
            // }  
            // sc.close();      
        } 
        catch (IOException | ParseError e) {
            System.err.println(e.getMessage());
        }
    }
}
