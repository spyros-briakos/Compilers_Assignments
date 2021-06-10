package src;
import syntaxtree.*;
import java.util.*;
import visitor .GJDepthFirst;

public class Third_Visitor extends GJDepthFirst<String,Class> {
    Deque<List<String>> expr_list;
    Symbol_Table table;
    Method current_method;

    public Third_Visitor(Symbol_Table table_) {
        this.expr_list = new ArrayDeque<List<String>>();
        this.table = table_;
        this.current_method = null;
    }

    /* CONTAINS: 
       1) ArrayType
       2) BooleanType
       3) IntegerType
       4) Identifier
       5) IntegerLiteral
       6) TrueLiteral
       7) FalseLiteral
       8) ClassDeclaration
       9) ClassExtendsDeclaration
       10) MainClass 
       11) MethodDeclaration
       12) AssignmentStatement 
       13) PrimaryExpression 
       14) AndExpression 
       15) CompareExpression 
       16) PlusExpression 
       17) MinusExpression
       18) TimesExpression 
       19) ThisExpression
       20) BracketExpression 
       21) NotExpression
       22) ArrayLookup
       23) ArrayLength
       24) MessageSend 
       25) ArrayAllocationExpression
       26) AllocationExpression
       27) ArrayAssignmentStatement
       28) ExpressionList 
       29) ExpressionTail --check if we don't need it
       30) ExpressionTerm 
       31) IfStatement
       32) WhileStatement
       33) PrintStatement
    */

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
    public String visit(IntegerType n, Class q) {
        return "int";
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n, Class q) {
        return n.f0.toString();
    }

    /**
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, Class q) throws Exception {
        return "int";
    }

    /**
    * f0 -> "true"
    */
    public String visit(TrueLiteral n, Class q) throws Exception {
        return "boolean";
    }

    /**
    * f0 -> "false"
    */
    public String visit(FalseLiteral n, Class q) throws Exception {
        return "boolean";
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
    public String visit(ClassDeclaration n, Class q) throws Exception {
        n.f0.accept(this,q);
        String ClassName = n.f1.accept(this,q);
        Class temp = this.table.Classes.get(ClassName);
        
        // if(this.current_method != null) {
        //     System.out.println("ClassDeclaration -> current_method:" + this.current_method.name + " current_class:" + temp.name);
        // }
        // else {
        //     System.out.println("ClassDeclaration -> current_method:" + this.current_method + " current_class:" + temp.name);
        // }

        n.f3.accept(this,temp);
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
        n.f0.accept(this,q);
        String ClassName = n.f1.accept(this,q);
        Class temp = this.table.Classes.get(ClassName);

        // if(this.current_method != null) {
        //     System.out.println("ClassExtendsDeclaration -> current_method:" + this.current_method.name + " current_class:" + temp.name);
        // }
        // else {
        //     System.out.println("ClassExtendsDeclaration -> current_method:" + this.current_method + " current_class:" + temp.name);
        // }

        n.f5.accept(this,temp);
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
        String ClassName = n.f1.accept(this,q);
        Class main_class = this.table.Classes.get(ClassName);
        this.current_method = main_class.Methods.get(0);
        
        // System.out.println("MainClass -> current_method:" + this.current_method.name + " current_class:" + main_class.name);

        n.f14.accept(this,main_class);
        n.f15.accept(this,main_class);

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
        String name = n.f2.accept(this,q);

        this.current_method = q.SearchMethod(name,table,false);

        // if(this.current_method != null) {
        //     System.out.println("MethodDeclaration -> current_method:" + this.current_method.name + " current_class:" + q.name);
        // }
        // else {
        //     System.out.println("MethodDeclaration -> current_method:" + this.current_method + " current_class:" + q.name);
        // }

        n.f7.accept(this,q);
        n.f8.accept(this,q);
        n.f9.accept(this,q);
        String return_value = n.f10.accept(this,q);
        
        // Case: Polymorphism 
        if(return_value != this.current_method.type) {
            if(!q.Polymorphism(return_value,this.current_method.type,this.table)) {
                throw new Exception("Error: Method " + this.current_method.name + " must have as return type: " + this.current_method.type + ", not " + return_value);
            }
        }      
        // Case: Same types, normal senario  
       
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
        String name = n.f0.accept(this,q);
        String expr = n.f2.accept(this,q);
        Variable var = null;

        if(q != null) {
            var = q.VarIsDeclared(name,this.table,this.current_method);
            if(var == null) {
                throw new Exception("Error: Variable " + name + " hasn't been declared!");
            }
        }
        else {
            throw new Exception("AssignmentStatement: Current Class Unexpected Problem!");            
        }
        
        // System.out.println("AssignmentStatement " + this.current_method.name + " " + q.name);

        // Case: Polymorphism 
        if(!var.type.equals(expr)) {
            if(!q.Polymorphism(expr,var.type,this.table)) {
                throw new Exception("Error: Mismatched types between " + var.type + " and " + expr + "!");
            }
        }

        return expr;
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
        String name = n.f0.accept(this,q);
    
        // Case: Identifier (Check if it has been declared before)
        if(n.f0.which == 3) {
            Variable temp_var = q.VarIsDeclared(name,this.table,this.current_method);
            if(temp_var != null) {
                return temp_var.type;
            }
            else {
                throw new Exception("Error: Variable " + name + " hasn't been declared!");
            }
        }
        // Other cases just accept (i.e. Trueliteral,FalseLiteral etc)

        return n.f0.accept(this,q); 
   }

     /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    public String visit(AndExpression n, Class q) throws Exception {
        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);
        if(t1 != "boolean" || t2 != "boolean") {
            throw new Exception("Error: With operator && you must use only boolean variables!");
        }
        return "boolean";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, Class q) throws Exception {
        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);
        if(t1 != "int" || t2 != "int") {
            throw new Exception("Error: With operator < you must use only integers!");
        }
        return "boolean";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, Class q) throws Exception {
        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);
        if(t1 != "int" || t2 != "int") {
            throw new Exception("Error: With operator + you must use only integers!");
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, Class q) throws Exception {
        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);
        if(t1 != "int" || t2 != "int") {
            throw new Exception("Error: With operator - you must use only integers!");
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    public String visit(TimesExpression n, Class q) throws Exception {
        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);
        if(t1 != "int" || t2 != "int") {
            throw new Exception("Error: With operator * you must use only integers!");
        }
        return "int";
    }


    /**
    * f0 -> "this"
    */
    public String visit(ThisExpression n, Class q) throws Exception {
        return q.name.toString();
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public String visit(BracketExpression n, Class q) throws Exception {
        return n.f1.accept(this,q);
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    public String visit(NotExpression n, Class q) throws Exception {
        String t = n.f1.accept(this,q);
        if(!t.equals("boolean")) {
            throw new Exception("Error: With operator ! you must use only boolean variable!");
        }
        return "boolean";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    public String visit(ArrayLookup n, Class q) throws Exception {
        String t1 = n.f0.accept(this,q);
        String t2 = n.f2.accept(this,q);
        if(t1 != "int[]") {
            throw new Exception("Error: Array type in MiniJava must be int[], not " + t1 + "!");
        }
        if(t2 != "int") {
            throw new Exception("Error: Indices of array in MiniJava must be integers, not " + t2 + "!");
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public String visit(ArrayLength n, Class q) throws Exception {
        String t = n.f0.accept(this,q);
        if(t != "int[]") {
            throw new Exception("Error: Attribute .length is applied on int[], not in " + t);
        }
        return "int";
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
        Class temp_class = null;
        String t = n.f0.accept(this,q);
        
        if(q != null) {
            // Case: Tried to apply as PrimaryExpression int or boolean or int[]
            if(!q.TypeIsObject(t)) {
                throw new Exception("Error: Could not apply . in " + t + "!");
            }
            // Case: Object of a random Class
            else
            {
                temp_class = table.Classes.get(t);
                if(temp_class == null) {
                    throw new Exception("Error: Undefined Class '" + t + "'!");
                }
            }
        }

        // if(this.current_method != null) {
        //     System.out.println("MessageSend -> current_method:" + this.current_method.name + " current_class:" + q.name);
        // }
        // else {
        //     System.out.println("MessageSend -> current_method:" + this.current_method + " current_class:" + q.name);
        // }

        String name_id = n.f2.accept(this,q);
        // Search this method everywhere
        Method temp_method = temp_class.SearchMethod(name_id,this.table,true);
        if(temp_method == null) {
            throw new Exception("Error: Undefined Method '" + name_id + "()'!");
        }

        // Brand new expr_list for new Message Send
        List<String> temp_list = new java.util.ArrayList<String>();
        this.expr_list.push(temp_list);

        // With this accept expr_list is gonna be filled with arguments of Message Send
        n.f4.accept(this,q);

        // System.out.println("MessageSend2 -> expr_list_size:" + this.expr_list.size() + " " + t);

        // Check if parameters are compatible between temp_method and what ExprList collected.
        temp_class.MessageSendExprList(temp_method,this.table,this.expr_list);

        // When ExpressionList is over, clear list which contains arguments
        this.expr_list.pop();

        return temp_method.type;
    }

    /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(ArrayAllocationExpression n, Class q) throws Exception {
        String t = n.f3.accept(this,q);
        if(t != "int") {
            throw new Exception("Error: Indices of array in MiniJava must be integers, not " + t + "!");
        }
        return "int[]";
    }

    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public String visit(AllocationExpression n, Class q) throws Exception {
        String ClassName = n.f1.accept(this,q);
        Class temp_class = this.table.Classes.get(ClassName);
        if(temp_class == null) {
            throw new Exception("Error: Problem in allocation with Class " + ClassName + "!");
        }
        return temp_class.name.toString();
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
        String id_name = n.f0.accept(this,q);
        String array_index = n.f2.accept(this,q);
        String expr = n.f5.accept(this,q);

        Variable temp_var = q.VarIsDeclared(id_name,this.table,this.current_method);
        if(temp_var == null) {
            throw new Exception("Error: Variable " + id_name + " hasn't been declared!");
        }
        if(temp_var.type != "int[]") {
            throw new Exception("Error: You must use int[] in Minijava, not " + temp_var.type + "!");
        }
        if(!array_index.equals("int")) {
            throw new Exception("Error: Only integers are allowed as indices of an array, not " + array_index + "!");
        }
        if(expr != "int") {
            throw new Exception("Error: Types mismatched, expected int,but found " + expr + "!");
        }

        return null;
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, Class q) throws Exception {
        String t = n.f0.accept(this,q);
        if(t != null) {
            //Insert it inside stack...
            this.expr_list.getFirst().add(t);
        }

        // System.out.println("ExpressionList -> Expr: " + t + " expr_list_size: " + this.expr_list.size());

        n.f1.accept(this,q);
        return null;
    }

    /**
    * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n, Class q) throws Exception {
        // String t = n.f0.accept(this,q);
        for(int j=0;j<n.f0.size();j++) {
            String t = n.f0.elementAt(j).accept(this,q);
            //Insert it inside buffer...
            this.expr_list.getFirst().add(t);
        }

        // System.out.println("ExpressionTail -> expr_list_size: " + this.expr_list.size());

        return null;
    }

    /**
    * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, Class q) throws Exception {
        String t = n.f1.accept(this,q);
        // if(t != null) {
        //     //Insert it inside buffer...
        //     this.expr_list.add(t);
        // }

        // System.out.println("ExpressionTerm -> expr_list_size: " + this.expr_list.size());

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
        String t = n.f2.accept(this,q);
        if(!t.equals("boolean")) {
            throw new Exception("Error: " + t + " is not accepted in: if(condition), you must use boolean!");
        }
        n.f4.accept(this,q);
        n.f6.accept(this,q);
        return t;
    }

    /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public String visit(WhileStatement n, Class q) throws Exception {
        String t = n.f2.accept(this,q);
        if(t != ("boolean")) {
            throw new Exception("Error: " + t + " is not accepted in: while(condition), you must use boolean!");
        }
        n.f4.accept(this,q);
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
        String t = n.f2.accept(this,q);
        if(!t.equals("int")) {
            throw new Exception("Error: PrintStatement accept only integers, not " + t);
        }
        return null;
    }
}