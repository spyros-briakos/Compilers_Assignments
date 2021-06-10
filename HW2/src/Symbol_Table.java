package src;
import java.util.*;

public class Symbol_Table {
    
    public Map<String,Class> Classes;    

    public Symbol_Table() {
        Classes = new HashMap<>();
    }

    public Class get_class(String c) {
        return Classes.get(c);
    }

    public void Print_Offsets() {
        Set<String> keys = Classes.keySet();

        for(String key : keys) {
            Classes.get(key).Print_Offset();
        }
    }
}
