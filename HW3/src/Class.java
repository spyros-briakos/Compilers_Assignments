package src;
import java.util.*;

public class Class {
    public String name;
    public String extend;
    public int Variable_Offset,Method_Offset;
    public List<Variable> Variables;
    public List<Method> Methods;
    boolean init;
    int vtable_size;

    public Class(String name_, String extend_) {
        this.name = name_;
        this.extend = extend_;
        this.Variable_Offset = 0;
        this.Method_Offset = 0;
        this.Variables = new ArrayList<>();
        this.Methods = new ArrayList<>();
        this.init = false;
        this.vtable_size=0;
    }

    public void Insert_Variable(Variable new_v, Symbol_Table symbol_table) throws Exception {
        for(Variable v: this.Variables) {
            if(v.name.equals(new_v.name)) {
                throw new Exception("Variable '" + new_v.name + "' already declared!");
            }
        }

        //------ Offset ------//
        Class temp = this;
        
        // Case: If parent exists get him and we are on first variable
        if(this.extend != null && this.Variables.size() == 0) {
            temp = symbol_table.Classes.get(this.extend);
        }
        
        // Assign offset of parent class into new variable
        new_v.offset = temp.Variable_Offset;

        // Add the appropriate amount of bytes 
        if(new_v.type == "int[]") {
            this.Variable_Offset = temp.Variable_Offset + 8;
        }
        else if(new_v.type == "int") {
            this.Variable_Offset = temp.Variable_Offset + 4;
        }
        else if(new_v.type == "boolean") {
            this.Variable_Offset = temp.Variable_Offset + 1;
        }
        else {
            this.Variable_Offset = temp.Variable_Offset + 8;
        }

        // Finally add new variable
        this.Variables.add(new_v);  
    }
    

    public boolean EqualParams(List<Variable> t1, List<Variable> t2) {
        if(t1.size() != t2.size()) {
            return false;
        }
        for(int j=0;j<t1.size();j++) {
            if(t1.get(j).type != t2.get(j).type) {
                return false;
            }
        }
        return true;
    }

    public void Insert_Method(Method method_, Symbol_Table symbol_table) throws Exception {
        Class parent_class = null, temp = null;
        if(this.extend != null) {
            parent_class = symbol_table.Classes.get(this.extend);
        }

        // if(parent_class != null) {
        //     System.out.println("Insert_Method " + method_.type + " " + method_.name + " " + parent_class.name);
        // }
        // else {
        //     System.out.println("Insert_Method " + method_.type + " " + method_.name + " " + parent_class);
        // }

        for(Method k : this.Methods) {
            if(k.name.equals(method_.name)) {
                throw new Exception("Method '" + method_.name + "' already declared!");
            }
        }

        //------ Offset ------//      
        boolean flag = false;
        while(parent_class != null) {
            for(Method temp_method : parent_class.Methods) {
                if(method_.name.equals(temp_method.name)) {
                    method_.override = true;
                    method_.offset = temp_method.offset;
                    flag = true;
                    // System.out.println(parent_class.name + "." + temp_method.name + " " + this.name + "." + method_.name);
                    break;
                }
            }   
            if(flag) {
                break;
            }
            // Retrieve next parent, if it exists
            if(parent_class.extend != null) {
                parent_class = symbol_table.Classes.get(parent_class.extend);
            }
            else {
                break;
            }    
        }
       
        // Case: If parent exists and we are on first method  
        // then grab parent's class MethodOffset and
        // if(this.extend != null && !init && !method_.override) {
        if(this.extend != null && !init) {
            temp = symbol_table.Classes.get(this.extend);            
            this.Method_Offset = temp.Method_Offset;
            
            if(!method_.override) {
                method_.offset = temp.Method_Offset;
                this.Method_Offset = this.Method_Offset + 8;
            }
            init = true;
        }
        // Case: Simple senario just move index 8 bytes 
        else if(method_.name != "main" && !method_.override) {
            method_.offset = this.Method_Offset;
            this.Method_Offset = this.Method_Offset + 8;
        }

        // System.out.println(this.name + "." + method_.name + " extends from " + this.extend + ", override:" + method_.override + ", offset: " + method_.offset + ", init:" + init + "\n");

        // Finally add new Method
        this.Methods.add(method_);
    }

    public Variable Retrieve_Variable(String name_, Symbol_Table symbol_table) {
        for(Variable var : this.Variables) {
            if(var.name.equals(name_)) {
                return var;
            }
        }

        if(this.extend != null) {
            return symbol_table.get_class(extend).Retrieve_Variable(name_,symbol_table);
        }

        return null;
    }

    public Method Retrieve_Method(String name_, Symbol_Table symbol_table) {
        for(Method method_ : this.Methods) {
            if(method_.name.equals(name_)) {
                return method_;
            }
        }

        if(this.extend != null) {
            return symbol_table.get_class(extend).Retrieve_Method(name_,symbol_table);
        }

        return null;
    }

    public void Print_Offset() {
    if((this.Variables.size() == 0) && (this.Methods.size() == 0 || (this.Methods.size() == 1 && this.Methods.get(0).name.equals("main")))) {}
    else {
        System.out.println("~~~~~~ Class " + this.name + " ~~~~~~");
        
        if(this.Variables.size() != 0) {
            System.out.println("\n----- Variables -----");
            for(Variable var: this.Variables) {
                System.out.println(this.name + "." + var.name + " : " + var.offset);
            }
            System.out.println("");
        }
        
        if(this.Methods.size() != 0 && !(this.Methods.size() == 1 && this.Methods.get(0).name.equals("main"))) {
            System.out.println("\n----- Methods -----");
            for(Method method_: this.Methods) {
                if(!method_.name.equals("main")) {
                    if(!method_.override){
                        System.out.println(this.name + "." + method_.name + " : " + method_.offset);
                    }
                }
            }
            System.out.println("");
        }
        
        for(int i=0;i<this.name.length();i++) {
            System.out.print("~");
        }
        System.out.print("~~~~~~~~~~~~~~~~~~~~\n\n");
    }
}
        
    public void HandleOverloading(Method method,Symbol_Table table) throws Exception {
        Class extended = table.Classes.get(this.extend);
        
        // if(extended != null) {
        //     System.out.println("HandleOverloading " + extended.name + " " + method.override);
        // }
        // else {
        //     System.out.println("HandleOverloading " + extended + " " + method.override);
        // }

        if(method.override) {
            while(extended != null) {
                Method overriden_method = extended.Retrieve_Method(method.name,table);
                
                if(overriden_method != null) {
                    if(!EqualParams(overriden_method.Parameters,method.Parameters) || !overriden_method.type.equals(method.type)) {        
                        throw new Exception("Detected Overloading (which is forbidden) between methods...\n" + method.Print() + "\n" + overriden_method.Print());
                    }
                }
                extended = table.Classes.get(extended.extend);
            }
        }
    }

    public void SearchVar(String name) throws Exception {
        for (Variable var : this.Variables) {
            if(var.name.equals(name)) {
                throw new Exception("Variable " + name + " is already defined!\n");
            }
        }
    }

    public Method SearchMethod(String name,Symbol_Table table,Boolean inherit) throws Exception {
        for (Method method : this.Methods) {
            if(method.name.equals(name)) {
                // throw new Exception("Method " + name + " is already defined!\n");
                return method;
            }
        }
        
        if(inherit) {
            if(this.extend != null) {
                return table.Classes.get(this.extend).SearchMethod(name,table,inherit);
            }
        }
        return null;
    }   

    public Variable GetVar(String var,List<Variable> mylist) {
        for(Variable temp : mylist) {
            if(temp.name == var) {
                return temp;
            }
        }
        return null;
    }

    public Variable GetVarInherit(String var, Symbol_Table table) {
        for(Variable temp : this.Variables) {
            if(temp.name == var) {
                return temp;
            }
        }
        if(this.extend != null) {
            return table.get_class(this.extend).GetVarInherit(var,table);
        }
        return null;
    }

    public Variable VarIsDeclared(String var, Symbol_Table table,Method current_method) throws Exception {
        Variable temp_var = GetVar(var,current_method.Variables);
        if(temp_var != null)    return temp_var;
        temp_var = GetVar(var,current_method.Parameters);
        if(temp_var != null)    return temp_var;
        temp_var = GetVar(var,this.Variables);
        if(temp_var != null)    return temp_var;

        Class temp_class = table.Classes.get(this.extend);
        while(temp_class != null) {
            temp_var = GetVar(var,temp_class.Variables);
            if(temp_var != null)    return temp_var;
            temp_class = table.Classes.get(temp_class.extend);
        }
        
        return null;
    }

    public boolean TypeIsObject(String t) {
        if(!t.equals("boolean") && !t.equals("int") && !t.equals("int[]")) {
            return true;
        }
        return false;
    }

    public boolean Polymorphism(String type1, String type2,Symbol_Table table) throws Exception {
        Class parent_class,sub_class;

        if(TypeIsObject(type1) && TypeIsObject(type2)) {
            parent_class = table.Classes.get(type2);
            sub_class = table.Classes.get(type1);
            
            // System.out.println("Polymorphism " + sub_class.name + " " + parent_class.name + " " + sub_class.extend);            
            
            while(sub_class != null) {
                    
                if(sub_class.extend != null) { 
                    //Case: sub_class inherits from parent 
                    if(sub_class.extend.equals(parent_class.name)) {
                        return true;
                    }
                }
                else {
                    return false;
                }

                //Keep going up to find parent of parent etc
                sub_class = table.Classes.get(sub_class.extend);
            }
        }
        return false;
    }

    public boolean MessageSendExprList(Method temp_method,Symbol_Table table,Deque<List<String>> expr_list) throws Exception {
        List<String> temp_list = expr_list.getFirst();

        if(temp_list.size() != temp_method.Parameters.size()) {
            throw new Exception("Error: Expected " + temp_method.Parameters.size() + " parameters,but found " + temp_list.size() + "!");
        }

        for (int j=0;j<temp_list.size();j++) {
            String t1 = temp_list.get(j);
            String t2 = temp_method.Parameters.get(j).type;

            // Case: Check for polymorphism
            if(!t1.equals(t2)) {
                if(!this.Polymorphism(t1,t2,table)) {
                    throw new Exception("Error: Mismatched types... Expected " + t2 + ",but found " + t1 + "!");
                }
            }
            // else
            // Case: Identical types, normal senario
        }
        return true;
    }
}
