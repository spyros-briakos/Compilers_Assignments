package src;
import syntaxtree.*;
import visitor .GJDepthFirst;

public class Second_Visitor extends GJDepthFirst<String,Class> {
    Symbol_Table table;
    Method current_method;

    public Second_Visitor(Symbol_Table table_) {
        this.table = table_;
        this.current_method = null;
    }

    /* CONTAINS: 
       1) ArrayType
       2) BooleanType
       3) IntegerType
       4) Identifier
       5) Type
       6) VarDeclaration
       7) MethodDeclaration
       8) FormalParameter
       9) ClassDeclaration
       10) ClassExtendsDeclaration
       11) MainClass
    */

    /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    public String visit(ArrayType n, Class q) {
        return "int[]";
    }

    /**
    * f0 -> "boolean"
    */
    public String visit(BooleanType n, Class q) {
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
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    public String visit(Type n, Class q) throws Exception{
        String type_ = n.f0.accept(this,q);
        if(!type_.equals("int[]") && !type_.equals("int") && !type_.equals("boolean")) {
            if(this.table.get_class(type_) == null) {
                throw new Exception(type_ + " cannot be used! Only int,int[],boolean or an existed Class object are permitted!");
            }
        }
        return type_;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, Class q) throws Exception {
        String type = n.f0.accept(this,q);
        String name = n.f1.accept(this,q);
        Variable var = new Variable(type,name);
        
        // if(this.current_method != null) {
        //     System.out.println("VarDeclaration -> current_var:" + var.name + " current_method:" + this.current_method.name + " current_class:" + q.name);
        // }
        // else {
        //     System.out.println("VarDeclaration -> current_var:" + var.name + " current_method:" + this.current_method + " current_class:" + q.name);
        // }

        if(this.current_method != null) {
            this.current_method.Insert_Variable(var);
        }
        else if(q != null) {
            q.Insert_Variable(var,this.table);
        }

        // System.out.println("VarDeclaration2");
        
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
        n.f0.accept(this,q);
        String type = n.f1.accept(this,q);
        String name = n.f2.accept(this,q);
        this.current_method = new Method(type,name);
        
        // if(this.current_method != null) {
        //     System.out.println("MethodDeclaration -> current_method:" + this.current_method.name + " current_class:" + q.name);
        // }
        // else {
        //     System.out.println("MethodDeclaration -> current_method:" + this.current_method + " current_class:" + q.name);
        // }
        
        q.Insert_Method(this.current_method,this.table);
        
        n.f4.accept(this,q);
                
        q.HandleOverloading(this.current_method,this.table);
        
        n.f7.accept(this,q);
        n.f8.accept(this,q);
        n.f10.accept(this,q);
        
        this.current_method = null;
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
    public String visit(FormalParameter n, Class q) throws Exception {
        String type = n.f0.accept(this,q);
        String name = n.f1.accept(this,q);
        Variable var = new Variable(type,name);
        
        // if(this.current_method != null) {
        //     System.out.println("FormalParameter -> current_method:" + this.current_method.name + " current_class:" + q.name);
        // }
        // else {
        //     System.out.println("FormalParameter -> current_method:" + this.current_method + " current_class:" + q.name);
        // }

        this.current_method.Insert_Parameter(var);

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

        String main = n.f6.accept(this,main_class);
        this.current_method = new Method("void","main");
        main_class.Insert_Method(this.current_method,this.table);
        
        // System.out.println("MainClass -> current_method:" + this.current_method.name + " current_class:" + main_class.name);

        String identifier = n.f11.accept(this,main_class);
        Variable var = new Variable("String[]",identifier);
        this.current_method.Insert_Parameter(var);

        n.f14.accept(this,main_class);
        n.f15.accept(this,main_class);

        this.current_method = null;
        return null;
    }
}
