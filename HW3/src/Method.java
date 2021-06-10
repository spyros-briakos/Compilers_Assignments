package src;
import java.util.*;

public class Method {

    public String type;
    public String name;        
    public int offset; 
    public boolean override;
    public String inherited_class;
    public List<Variable> Parameters,Variables;

    public Method(String type_, String name_) {
        this.type = type_;
        this.name = name_;
        this.offset = 0;
        this.override = false;
        this.inherited_class = "";
        this.Parameters = new ArrayList<>();
        this.Variables = new ArrayList<>();
    }
    
    public void Insert_Parameter(Variable new_v) throws Exception {
        for(Variable v: this.Parameters) {
            if(v.name.equals(new_v.name)) {
                throw new Exception("Parameter '" + new_v.name + "' already declared!");
            }
        }
        this.Parameters.add(new_v);
    }

    public Variable Retrieve_Variable(Variable new_v) {
        for(Variable v: this.Parameters) {
            if(v.name.equals(new_v.name)) {
                return v;
            }
        }
        for(Variable v: this.Variables) {
            if(v.name.equals(new_v.name)) {
                return v;
            }
        }
        return null;
    }

    public void Insert_Variable(Variable new_v) throws Exception {
        for(Variable v: this.Parameters) {
            if(v.name.equals(new_v.name)) {
                throw new Exception("Variable '" + new_v.name + "' already declared!!");
            }
        }
        for(Variable v: this.Variables) {
            if(v.name.equals(new_v.name)) {
                throw new Exception("Variable '" + new_v.name + "' already declared!");
            }
        }
        this.Variables.add(new_v);
    }

    public String Print() {
        String ret = "";
        ret = ret + (this.type + " " + this.name + "(").toString();

        int size = this.Parameters.size();
        if( size != 0 ) {
            ret = ret + (this.Parameters.get(0).type).toString() + " " + (this.Parameters.get(0).name).toString();

            for(int j=1;j<size;j++) {
                ret = ret + ", " + (this.Parameters.get(j).type).toString() + " " + (this.Parameters.get(j).name).toString();
            }
        }
        ret = ret + (")").toString();

        return ret;
    }

    public Variable GetVar(String var_name) {
        for(Variable temp_var : this.Variables) {
            if(temp_var.name.equals(var_name)) {
                return temp_var;
            }
        }
        for(Variable temp_var : this.Parameters) {
            if(temp_var.name.equals(var_name)) {
                return temp_var;
            }
        }
        return null;
    }
}