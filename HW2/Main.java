import src.*;
import syntaxtree.*;
import java.io.*;

class Main {
    public static void main (String [] args) {
        
        if(args.length == 0) {
            System.err.println("Usage: java Main [file1] [file2] ... [fileN]");
            System.exit(1);
        }
        
        FileInputStream fis = null;
        
        for(String filename : args) {
            try {
                System.out.print("\n############");
                for(int i=0;i<filename.length();i++) {
                    System.out.print("#");
                }
                System.out.print("\n# File: '" + filename + "' #\n");
                for(int i=0;i<filename.length();i++) {
                    System.out.print("#");
                }
                System.out.println("############");

                fis = new FileInputStream(filename);
                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();
                Symbol_Table table = new Symbol_Table();
                
                First_Visitor vis1 = new First_Visitor(table);
                root.accept(vis1,null);
                
                // System.out.println("\n##########################################################################################################\n");

                Second_Visitor vis2 = new Second_Visitor(table);
                root.accept(vis2,null);
                
                // System.out.println("\n##########################################################################################################\n");

                Third_Visitor vis3 = new Third_Visitor(table);
                root.accept(vis3,null);

                // System.out.println("\n##########################################################################################################\n");
                
                table.Print_Offsets();
            }
            catch(ParseException ex) {
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            }
            catch(Exception ex) {
                System.out.println(ex);
            }
        }
    }
}