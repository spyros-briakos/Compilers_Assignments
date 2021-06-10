package src;
import syntaxtree.*;
import java.io.BufferedWriter;
import java.util.*;
import visitor .GJDepthFirst;

public class LLVM_Visitor extends GJDepthFirst<String,Class> {
    Deque<List<String>> expr_list;
    Symbol_Table table;
    Method current_method;
    BufferedWriter llvm_writer;
    int var_counter;   // counter to keep number of current variable
    int label_counter; // counter to keep number of current label
    String AssignOrNot;
    String MessageSendClass;
    boolean messagesendflag;

    public LLVM_Visitor(Symbol_Table table_, BufferedWriter writer_) throws Exception {
        this.expr_list = new ArrayDeque<List<String>>();
        this.table = table_;
        this.current_method = null;
        this.var_counter = 0;
        this.label_counter = 0;
        this.llvm_writer = writer_;
        this.AssignOrNot = ""; //default value (toggle to "Yes" only in AssignmentStatement and "No" in many other cases)
        this.MessageSendClass = "";
        this.messagesendflag = false;

        this.Init_VTables();
    }

    /* CONTAINS: 
       1)  ✅ ArrayType 
       2)  ✅ BooleanType 
       3)  ✅ IntegerType 
       4)  ✅ Identifier   
       5)  ✅ IntegerLiteral 
       6)  ✅ TrueLiteral 
       7)  ✅ FalseLiteral 
       8)  ✅ ClassDeclaration 
       9)  ✅ ClassExtendsDeclaration 
       10) ✅ MainClass  
       11) ✅ MethodDeclaration 
       12) ✅ AssignmentStatement 
       13) ✅ PrimaryExpression 
       14) ✅ AndExpression 
       15) ✅ CompareExpression  
       16) ✅ PlusExpression  
       17) ✅ MinusExpression 
       18) ✅ TimesExpression 
       19) ✅ ThisExpression 
       20) ✅ BracketExpression 
       21) ✅ NotExpression 
       22) ✅ ArrayLookup 
       23) ✅ ArrayLength 
       24) ✅ MessageSend  
       25) ✅ ArrayAllocationExpression 
       26) ✅ AllocationExpression 
       27) ✅ ArrayAssignmentStatement 
       28) ✅ ExpressionList 
       29) ✅ ExpressionTail
       30) ✅ ExpressionTerm 
       31) ✅ IfStatement 
       32) ✅ WhileStatement 
       33) ✅ PrintStatement 
    */

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void Init_VTables() throws Exception {
        Set<String> keys = this.table.Classes.keySet();
        for(String key: keys) {
            Class temp = this.table.get_class(key);
            
            // Case: Main Class
            Method maybemain = temp.SearchMethod("main",this.table,false);
            if(maybemain != null) {
                output("\n@." + temp.name + "_vtable = global [ 0  x i8*] []");
                continue;
            }
            
            // Case: Normal Class
            this.VtableClass(temp);
        }

        // Output constant lines of LLVM IR code
        output("\n\ndeclare i8* @calloc(i32, i32)\n");
        output("declare i32 @printf(i8*, ...)\n");
        output("declare void @exit(i32)\n\n");
        output("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n");
        output("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n");
        output("define void @print_int(i32 %i) {\n");
        output("    %_str = bitcast [4 x i8]* @_cint to i8*\n");
        output("    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n");
        output("    ret void\n}\n\n");
        output("define void @throw_oob() {\n");
        output("    %_str = bitcast [15 x i8]* @_cOOB to i8*\n");
        output("    call i32 (i8*, ...) @printf(i8* %_str)\n");
        output("    call void @exit(i32 1)\n");
        output("    ret void\n}\n");
    }

    public void VtableClass(Class temp) throws Exception {
        Map<Integer,Method> Methods_Of_Class = new HashMap<>();

        print("Class " + temp.name);
        
        // Insert Class's Methods
        for(Method method : temp.Methods) {
            print(method.name + " " + method.offset);
            Methods_Of_Class.put(method.offset,method);
        }
        // Search in Parents Classes 
        if(temp.extend!=null) {
            Class parent = this.table.get_class(temp.extend);
            
            while(parent != null) {
                for(Method method : parent.Methods) {
                    print("Parent " + method.name + " " + method.offset);
                    
                    if(!Methods_Of_Class.containsKey(method.offset)) {
                        method.inherited_class = parent.name;
                        Methods_Of_Class.put(method.offset,method);
                    }
                }
                parent = this.table.get_class(parent.extend);
            }
        }

        SortedSet<Integer> keySet = new TreeSet<>(Methods_Of_Class.keySet());

        print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        temp.vtable_size = keySet.size();
        output("\n@." + temp.name + "_vtable = global [" + temp.vtable_size + " x i8*] [");
        
        // Iterate through Class's methods
        for(Integer key : keySet) {
            Method m = Methods_Of_Class.get(key);
            
            if(!m.inherited_class.equals("")) {
                print(m.inherited_class + "." + m.name + ", offset:" + m.offset);
            }
            else {
                print(temp.name + "." + m.name + ", offset:" + m.offset);
            }
            
            String type = toLLVM(m.type);
            output("i8* bitcast (" + type + " (i8*");
            
            // Iterate through Method's Parameters
            for(Variable temp_var : m.Parameters) {
                output("," + toLLVM(temp_var.type));
            }

            if(!m.inherited_class.equals("")) {
                output(")* @" + m.inherited_class + "." + m.name + " to i8*)");
            }
            else {
                output(")* @" + temp.name + "." + m.name + " to i8*)");
            }
            
            if(keySet.last() != key) {
                output(", ");                    
            }
        }
        output("]");
        print("------------------------------------\n");
    }

    public String toLLVM(String t) {
      if(t.equals("int")) {
        return "i32";
      }
      else if(t.equals("boolean")) {
        return "i1";
      }
      else if(t.equals("int[]")) {
        return "i32*";
      } 
      else { 
        return "i8*";
      }   
    }

    public String newRegLabel(boolean RegOrLabel) throws Exception {
      String t;
      
      // Case: True -> Reg
      if(RegOrLabel) {
        t = "%_" + this.var_counter;
        this.var_counter += 1;
      }
      // Case: False -> Label
      else {
        t = "%_lb" + this.label_counter;
        this.label_counter += 1;
      }      
      return t;
    }

    public void output(String t) throws Exception {
        this.llvm_writer.write(t);
        this.llvm_writer.flush();
    }

    public void print(String t) throws Exception {
        // System.out.println(t);
    }

    public String YesAssign(String name, Class q) throws Exception {
        Variable temp_var = this.current_method.GetVar(name);
        print("YesAssign");
        
        //Case: Pointer is member of Method
        if(temp_var != null) {
            print("here1");
            return "";
        }
        //Case: Pointer is member of Class
        else {
            print("here2");
            temp_var = q.GetVarInherit(name,this.table);
            String new_reg1 = newRegLabel(true);
            String new_reg2 = newRegLabel(true);
            String type = toLLVM(temp_var.type);
            
            output("\t" + new_reg1 + " = getelementptr i8, i8* %this, i32 " + (8 + temp_var.offset) + "\n");
            output("\t" + new_reg2 + " = bitcast i8* " + new_reg1 + " to " + type + "*\n");
            return new_reg2;
        }
    }
    
    public String NoAssign(String name, Class q) throws Exception {
        print("NoAssign");
        Variable temp_var = this.current_method.GetVar(name);
    
        String type;
        //Case: Variable is member of Method
        if(temp_var != null) {
            print("here1");
            String new_reg = newRegLabel(true);
            type = toLLVM(temp_var.type);
            output("\t" + new_reg + " = load " + type + ", " + type + "* %" + temp_var.name + "\n");
            return new_reg;
        }
        //Case: Variable is member of Class
        else {
            print("here2");
            Variable temp_var1 = q.GetVarInherit(name,this.table);
            // print("ERROR? " + q.name + " " + temp_var1 + " " +  name);
            String new_reg1 = newRegLabel(true);
            String new_reg2 = newRegLabel(true);
            String new_reg3 = newRegLabel(true);
            type = toLLVM(temp_var1.type);
            
            output("\t" + new_reg1 + " = getelementptr i8, i8* %this, i32 " + (8 + temp_var1.offset) + "\n");
            output("\t" + new_reg2 + " = bitcast i8* " + new_reg1 + " to " + type + "*\n");
            output("\t" + new_reg3 + " = load " + type + ", " + type + "* " + new_reg2 + "\n");
            return new_reg3;
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    public String visit(ArrayType n, Class q) throws Exception {
        return "int[]";    
    }

    /**
    * f0 -> "boolean"
    */
    public String visit(BooleanType n, Class q) throws Exception {
       return "boolean";
    }

    /**
    * f0 -> "int"
    */
    public String visit(IntegerType n, Class q) throws Exception {
        return "int";
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n, Class q) throws Exception{
        String name = n.f0.tokenImage,res;

        print("Identifier " + this.AssignOrNot + " " + this.messagesendflag + " " + name);

        // Store Name of Class, cause we are gonna need this later
        this.MessageSendClass = name;

        // Special Case
        if(this.messagesendflag) {
            print("special message send case " + name);
            return name;
        }

        // Case: Not AssignmentStatement
        if(this.AssignOrNot.equals("No")) {
            res = this.NoAssign(name,q);
            print("1st case " + res);
        }
        // Case: AssignmentStatement
        else if(this.AssignOrNot.equals("Yes")) {
            res = this.YesAssign(name,q);
            if(res.equals("")) {
                print("Special");
                res = "%" + name;
            }
            print("2nd case " + res);
        }
        // Case: 
        else {
            res = name;
            print("3rd case " + res);
        }
        return res;
    }

    /**
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, Class q) throws Exception {
        String new_reg = newRegLabel(true);
        
        output("\n\t;IntegerLiteral");
        output("\n\t" + new_reg + " = add i32 0, " + n.f0.toString() + "\n");
        
        return new_reg;
    }

    /**
    * f0 -> "true"
    */
    public String visit(TrueLiteral n, Class q) throws Exception {
        return "1";
    }

    /**
    * f0 -> "false"
    */
    public String visit(FalseLiteral n, Class q) throws Exception {
        return "0";
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    public String visit(ClassDeclaration n, Class q) throws Exception {
        String ClassName = n.f1.accept(this,q);
        Class temp = this.table.Classes.get(ClassName);
        
        print("ClassDeclaration");
        n.f4.accept(this,temp);
        return null;
    }

    /*
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    public String visit(ClassExtendsDeclaration n, Class q) throws Exception {
        String ClassName = n.f1.accept(this,q);
        Class temp = this.table.Classes.get(ClassName);

        print("ClassExtendsDeclaration");
        n.f6.accept(this,temp);
        return null;
    }

    /*
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
    public String visit(MainClass n, Class q) throws Exception {
        print("MainClass");
        String ClassName = n.f1.accept(this,q);
        Class main_class = this.table.Classes.get(ClassName);
        this.current_method = main_class.Methods.get(0);
        
        output("\ndefine i32 @main() {\n");
        
        for(Variable temp_var : this.current_method.Variables) {
            output("\t%" + temp_var.name + " = alloca " + toLLVM(temp_var.type) + "\n");
        }
        
        // Statement
        n.f15.accept(this,main_class);
        output("\n\n\tret i32 0\n}");
        this.current_method = null;
        return null;
    }

    /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
    public String visit(MethodDeclaration n, Class q) throws Exception {
        String type = n.f1.accept(this,q);
        String name = n.f2.accept(this,q);
        print("MethodDeclaration " + type + " " + name);

        this.current_method = q.SearchMethod(name,table,false);

        output("\n\ndefine " + toLLVM(this.current_method.type) + " @" + q.name + "." + this.current_method.name + "(i8* %this");
        
        int parameters_size = this.current_method.Parameters.size();
        if(parameters_size != 0) {
            output(", ");

            // Iterate through Method's Parameters
            for(Variable temp_param : this.current_method.Parameters) {
                output(toLLVM(temp_param.type) + " %." + temp_param.name);
                if(this.current_method.Parameters.indexOf(temp_param) + 1 != parameters_size) {
                    output(", ");
                }
            }
        }
        output(") {\n");
        
        // Alloca and store for each Parameter
        for(Variable temp_param : this.current_method.Parameters) {
            String param_type = toLLVM(temp_param.type);
            output("\t%" + temp_param.name + " = alloca " + param_type + "\n\tstore " + param_type + " %." + temp_param.name + " ," + param_type + "* %" + temp_param.name + "\n");
        }
        // Alloca for each Variable 
        for(Variable temp_var : this.current_method.Variables) {
            String var_type = toLLVM(temp_var.type);
            output("\t%" + temp_var.name + " = alloca " + var_type + "\n");
        }
        
        n.f8.accept(this,q);
        this.AssignOrNot="No";
        output("\n\tret " + toLLVM(type) + " " + n.f10.accept(this,q) + "\n}\n");
        this.AssignOrNot="";
        this.current_method = null;
        return null;
    }

    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    public String visit(AssignmentStatement n, Class q) throws Exception {
        output("\n\t;AssignmentStatement\n");
        print("AssignmentStatement");
        String name = n.f0.accept(this,q);
        
        Variable temp_var = this.current_method.GetVar(name);
        if(temp_var == null) {
            temp_var = q.GetVarInherit(name,table);
        }

        this.AssignOrNot="No";
        String expr = n.f2.accept(this,q);        
        this.AssignOrNot="Yes";
        String name_ = n.f0.accept(this,q);
        this.AssignOrNot="";
        String var_type = toLLVM(temp_var.type);
        // print("HEY ASSIGN " + name + " " + expr + " " + name_ + " " + var_type);

        output("\tstore " + var_type + " " + expr + ", " + var_type + "* " + name_ + "\n");

        return null;
    }

    /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
    public String visit(PrimaryExpression n, Class q) throws Exception {
        // Case: Identifier 
        if(n.f0.which == 3) {          
            this.AssignOrNot = "No";
            String ident = n.f0.accept(this,q);
            this.AssignOrNot = "";
            return ident;
        }
        // Other cases just accept (i.e. Trueliteral,FalseLiteral etc)

        return n.f0.accept(this,q).toString(); 
    }

     /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    public String visit(AndExpression n, Class q) throws Exception {
        print("AndExpression");
        String label_if1 = newRegLabel(false);
        String label_if2 = newRegLabel(false);
        String label_if3 = newRegLabel(false);
        String label_if4 = newRegLabel(false);
        String new_reg = newRegLabel(true);
        
        this.AssignOrNot = "No";
        String t1 = n.f0.accept(this,q);
        this.AssignOrNot = "";

        output("\n\tbr label " + label_if1 + "\n");
        output("\t" + label_if1.substring(1).concat(":") + "\n");
        output("\tbr i1 " + t1 + ", label " + label_if2 + ", label " + label_if4 + "\n");
        output("\t" + label_if2.substring(1).concat(":") + "\n");

        this.AssignOrNot = "No";
        String t2 = n.f2.accept(this,q);
        this.AssignOrNot = "";

        output("\tbr label " + label_if3 + "\n");
        output("\t" + label_if3.substring(1).concat(":") + "\n");
        output("\tbr label " + label_if4 + "\n");
        output("\t" + label_if4.substring(1).concat(":") + "\n");
        output("\t" + new_reg + " = phi i1 [ 0, " + label_if1 +" ], [ " + t2 + ", " + label_if3 + " ]\n");
        
        return new_reg;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, Class q) throws Exception {
        print("CompareExpression");
        
        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);
        String new_reg = newRegLabel(true);

        output("\t" + new_reg + " = icmp slt i32 " + t1 + ", " + t2 + "\n");
        return new_reg;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, Class q) throws Exception {
        print("PlusExpression");
        output("\n\t;PlusExpression\n");

        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);

        String new_reg = newRegLabel(true);
        output("\t" + new_reg + " = add i32 " + t1 + ", " + t2 + "\n");
        
        return new_reg;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, Class q) throws Exception {
        print("MinusExpression");

        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);

        String new_reg = newRegLabel(true);
        output("\t" + new_reg + " = sub i32 " + t1 + ", " + t2 + "\n");
        
        return new_reg;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    public String visit(TimesExpression n, Class q) throws Exception {
        print("TimesExpression");

        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);

        String new_reg = newRegLabel(true);
        output("\t" + new_reg + " = mul i32 " + t1 + ", " + t2 + "\n");
        
        return new_reg;
    }


    /**
    * f0 -> "this"
    */
    public String visit(ThisExpression n, Class q) throws Exception {
        print("ThisExpression");

        // Store Name of Class, cause we are gonna need this later
        this.MessageSendClass = q.name;
        return "%this";
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public String visit(BracketExpression n, Class q) throws Exception {
        print("BracketExpression");

        return n.f1.accept(this,q);
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    public String visit(NotExpression n, Class q) throws Exception {
        print("NotExpression");

        String t = n.f1.accept(this,q);
        String new_reg = newRegLabel(true);
        output("\t" + new_reg + " = xor i1 1, " + t + "\n");
        
        return new_reg;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    public String visit(ArrayLookup n, Class q) throws Exception {
        output("\n\t;ArrayLookup\n");
        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);

        print("ArrayLookup " + t1 + " " + t2);

        String new_reg0 = newRegLabel(true);
        String new_reg1 = newRegLabel(true);
        String new_reg2 = newRegLabel(true);
        String new_reg3 = newRegLabel(true);
        String new_reg4 = newRegLabel(true);

        String label1 = newRegLabel(false);
        String label2 = newRegLabel(false);
        String label3 = newRegLabel(false);

        output("\t" + new_reg0 + " = load i32, i32* " + t1 + "\n");
        output("\t" + new_reg1 + " = icmp ult i32 " + t2 + ", " + new_reg0);
        output("\n\tbr i1 " + new_reg1 + ", label " + label1 + ", label " + label2 + "\n");
        output("\t" + label1.substring(1).concat(":") + "\n");
        output("\t" + new_reg2 + " = add i32 " + t2 + ", 1\n");
        output("\t" + new_reg3 + " = getelementptr i32, i32* " + t1 + ", i32 " + new_reg2 + "\n");
        output("\t" + new_reg4 + " = load i32, i32* " + new_reg3 + "\n");
        output("\tbr label " + label3 + "\n");
        output("\t" + label2.substring(1).concat(":") + "\n");
        output("\tcall void @throw_oob()\n");
        output("\tbr label " + label3 + "\n");
        output("\t" + label3.substring(1).concat(":") + "\n");

        return new_reg4;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public String visit(ArrayLength n, Class q) throws Exception {
        print("ArrayLength");

        String t = n.f0.accept(this,q);

        String new_reg = newRegLabel(true);
        output("\t" + new_reg + " = load i32, i32* " + t + "\n");
        return new_reg;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    public String visit(MessageSend n, Class q) throws Exception {

        String call = n.f0.accept(this,q);

        // Learn which object calls message send...
        String objectclass = this.MessageSendClass;
        print("Beginning Of MessageSend " + this.MessageSendClass);
        // Here is gonna be stored object's Class which called MessageSend
        Class theclass = null;
        
        // Search if it is declared inside Method
        Variable temp_var = this.current_method.GetVar(objectclass);

        // If it doesn't exist in method search in Class
        if(temp_var == null) {
            temp_var = q.GetVarInherit(objectclass,table);
            
            // Case: We found it as a data member of Class
            if(temp_var != null) {
                theclass = this.table.get_class(temp_var.type);
            }
            
            // If it doesn't exist as a variable neither in Method nor in Class, case this or allocation expression 
            if(temp_var == null) {
                theclass = this.table.get_class(objectclass);
            }
        }
        // It is declared inside method
        else {
            theclass = this.table.get_class(temp_var.type);
        }
        
        output("\n\t;MessageSend\n");
        print("MessageSend " + q.name + " " + objectclass + " " + theclass.name);
        
        String new_reg0 = newRegLabel(true);
        String new_reg1 = newRegLabel(true);
        String new_reg2 = newRegLabel(true);
        String new_reg3 = newRegLabel(true);
        String new_reg4 = newRegLabel(true);
        String new_reg5 = newRegLabel(true);

        output("\t" + new_reg0 + " = bitcast i8* " + call + " to i8***\n"); 
        output("\t" + new_reg1 + " = load i8**, i8*** " + new_reg0 + "\n");

        this.messagesendflag = true;
        String methodName = n.f2.accept(this,q);
        this.messagesendflag = false;
        print("check it " + methodName);

        Method themethod = theclass.Retrieve_Method(methodName,this.table);
        String methodtype = toLLVM(themethod.type);

        print("OFFSET " + theclass.name + "." + methodName + " " + themethod.offset);
    
        output("\t" + new_reg2 + " = getelementptr i8*, i8** " + new_reg1 + ", i32 " + themethod.offset/8 + "\n"); 
        output("\t" + new_reg3 + " = load i8*, i8** " + new_reg2 + "\n");
        output("\t" + new_reg4 + " = bitcast i8* " + new_reg3 + " to " + methodtype + "(i8*"); 
        
        for(Variable temp_param : themethod.Parameters) {
            output("," + toLLVM(temp_param.type));
        }
        output(")*\n");
        
        // Brand new expr_list for new Message Send
        List<String> temp_list = new java.util.ArrayList<String>();
        this.expr_list.push(temp_list);

        // With this accept expr_list is gonna be filled with arguments of Message Send
        this.AssignOrNot = "No";
        n.f4.accept(this,q);
        this.AssignOrNot = "";
        
        // When ExpressionList is over, clear list which contains arguments
        temp_list = this.expr_list.pop();
        
        output("\t" + new_reg5 + " = call " + methodtype + " " + new_reg4 + "(i8* " + call);

        for(int j=0;j<temp_list.size();j++) {
            String type_ = toLLVM(themethod.Parameters.get(j).type);
            String name_ = temp_list.get(j);
            print("-> " + type_ + " " + name_);
            output(", " + type_ + " " + name_);
        }
        output(")\n");

        this.MessageSendClass = themethod.type;
        return new_reg5;
    }

    /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(ArrayAllocationExpression n, Class q) throws Exception {
        String array_size = n.f3.accept(this,q);
        print("ArrayAllocationExpression " + array_size);
        output("\n\t;ArrayAllocationExpression\n");

        String new_reg0 = newRegLabel(true);
        String new_reg1 = newRegLabel(true);
        String new_reg2 = newRegLabel(true);
        String new_reg3 = newRegLabel(true);
        String label1 = newRegLabel(false);
        String label2 = newRegLabel(false);

        output("\t" + new_reg0 + " = add i32 1, " + array_size + "\n");
        output("\t" + new_reg1 + " = icmp sge i32 " + new_reg0 + ", 1\n");
        output("\tbr i1 " + new_reg1 + ", label " + label1 + ", label " + label2 + "\n");
        output("\t" + label2.substring(1).concat(":") + "\n");
        output("\tcall void @throw_oob()\n");
        output("\tbr label " + label1 + "\n");
        output("\t" + label1.substring(1).concat(":") + "\n");
        output("\t" + new_reg2 + " = call i8* @calloc(i32 " + new_reg0 + ", i32 4)\n");
        output("\t" + new_reg3 + " = bitcast i8* " + new_reg2 + " to i32*\n");
        output("\tstore i32 " + array_size + ", i32* " + new_reg3 + "\n");
        
        return new_reg3;
    }

    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public String visit(AllocationExpression n, Class q) throws Exception {
        output("\n\t;AllocationExpression\n");
        
        this.AssignOrNot = "";
        String ClassName = n.f1.accept(this,q);
        this.MessageSendClass = ClassName;
        Class temp_class = this.table.Classes.get(ClassName);
        
        print("AllocationExpression " + temp_class.name);
        
        String new_reg1 = newRegLabel(true);
        String new_reg2 = newRegLabel(true);
        String new_reg3 = newRegLabel(true);
        
        output("\t" + new_reg1 + " = call i8* @calloc(i32 1, i32 " + (temp_class.Variable_Offset + 8) + ")\n");
        output("\t" + new_reg2 + " = bitcast i8* " + new_reg1 + " to i8***\n");
        output("\t" + new_reg3 + " = getelementptr [" + temp_class.vtable_size + " x i8*], [" + temp_class.vtable_size + " x i8*]* @." + ClassName + "_vtable, i32 0, i32 0\n");
        output("\tstore i8** " + new_reg3 + ", i8*** " + new_reg2 + "\n");
        
        return new_reg1;
    }

    /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    public String visit(ArrayAssignmentStatement n, Class q) throws Exception {
        output("\n\t;ArrayAssignmentStatement\n");
        
        this.AssignOrNot="No";
        String id_name = n.f0.accept(this,q);
        this.AssignOrNot="";
        
        print("ArrayAssignmentStatement " + id_name);
        
        String new_reg1 = newRegLabel(true);
        String new_reg2 = newRegLabel(true);
        String new_reg3 = newRegLabel(true);
        String new_reg4 = newRegLabel(true);
        String label1 = newRegLabel(false);
        String label2 = newRegLabel(false);
        String label3 = newRegLabel(false);

        output("\t" + new_reg1 + " = load i32, i32* " + id_name + "\n");

        this.AssignOrNot="No";
        String array_index = n.f2.accept(this,q);
        String expr = n.f5.accept(this,q);
        this.AssignOrNot="";

        print("ArrayAssignmentStatement2 " + array_index);

        output("\t" + new_reg2 + " = icmp ult i32 " + array_index + ", " + new_reg1 + "\n");
        
        output("\n\tbr i1 " + new_reg2 + ", label " + label1 + ", label " + label2 + "\n");
        
        output("\t" + label1.substring(1).concat(":") + "\n");
        output("\t" + new_reg3 + " = add i32 " + array_index + ", 1\n");
        output("\t" + new_reg4 + " = getelementptr i32, i32* " + id_name + ", i32 " + new_reg3 + "\n");
        output("\tstore i32 " + expr + ", i32* " + new_reg4 + "\n");
        output("\tbr label " + label3 + "\n");

        output("\t" + label2.substring(1).concat(":") + "\n");
        output("\tcall void @throw_oob()\n");
        output("\tbr label " + label3 + "\n");

        output("\t" + label3.substring(1).concat(":") + "\n");

        return null;
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, Class q) throws Exception {
        print("ExpressionList");
        
        this.AssignOrNot = "No";
        String t = n.f0.accept(this,q);
        this.AssignOrNot = "";

        if(t != null) {
            //Insert it inside stack...
            this.expr_list.getFirst().add(t);
        }

        n.f1.accept(this,q);
        return null;
    }

    /**
    * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n, Class q) throws Exception {
        print("ExpressionTail");

        for(int j=0;j<n.f0.size();j++) {
            String t = n.f0.elementAt(j).accept(this,q);
            //Insert it inside buffer...
            this.expr_list.getFirst().add(t);
        }

        return null;
    }

    /**
    * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, Class q) throws Exception {
        print("ExpressionTerm");

        this.AssignOrNot = "No";
        String t = n.f1.accept(this,q);
        this.AssignOrNot = "";

        return t;
    }

    /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    public String visit(IfStatement n, Class q) throws Exception {
        print("IfStatement");
        
        this.AssignOrNot="No";
        String t = n.f2.accept(this,q);
        this.AssignOrNot="";

        String label_if = this.newRegLabel(false);
        String label_else = this.newRegLabel(false);
        String label_end = this.newRegLabel(false);

        output("\n\tbr i1 " + t + ", label " + label_if + ", label " + label_else + "\n");
        // Label If
        output("\t" + label_if.substring(1).concat(":") + "\n");
        n.f4.accept(this,q);
        output("\tbr label " + label_end + "\n\n");
        // Label Else
        output("\t" + label_else.substring(1).concat(":") + "\n");
        n.f6.accept(this,q);
        output("\tbr label " + label_end + "\n\n");
        // Label End
        output("\t" + label_end.substring(1).concat(":"));
        
        return null;
    }

    /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public String visit(WhileStatement n, Class q) throws Exception {
        print("WhileStatement");

        String label_while = newRegLabel(false);
        String label_continue = newRegLabel(false);
        String label_end = newRegLabel(false);

        output("\n\tbr label " + label_while);
        output("\n\t" + label_while.substring(1).concat(":"));
        
        this.AssignOrNot="No";
        String t = n.f2.accept(this,q);
        this.AssignOrNot="";

        output("\n\tbr i1 " + t + ", label " + label_continue + ", label " + label_end);
        output("\n\t" + label_continue.substring(1).concat(":"));
        n.f4.accept(this,q);
        output("\n\tbr label " + label_while);
        output("\n\t" + label_end.substring(1).concat(":"));
        return null;
    }

    /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    public String visit(PrintStatement n, Class q) throws Exception {
        print("PrintStatement");

        this.AssignOrNot="No";
        String t = n.f2.accept(this,q);
        this.AssignOrNot="";

        output("\tcall void (i32) @print_int(i32 " + t + ")" + "\n");
        return null;
    }
}