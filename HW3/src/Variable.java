package src;

public class Variable {
    public String type;
    public String name;
    public int offset;
    
    public Variable(String type_, String name_) {
        this.type = type_;
        this.name = name_;
        this.offset = 0;
    }
}