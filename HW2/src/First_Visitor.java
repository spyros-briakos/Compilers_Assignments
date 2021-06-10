package src;
import syntaxtree.*;
import visitor .GJDepthFirst;

public class First_Visitor extends GJDepthFirst<String,Class> {
    Symbol_Table table;
    Method current_method;

    public First_Visitor(Symbol_Table table_) {
        this.table = table_;
        this.current_method = null;
    }

    /* CONTAINS:
       1) MainClass
       2) Identifier
       3) ClassDeclaration
       4) ClassExtendsDeclaration 
    */

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
        n.f0.accept(this,q);
        String ClassName = n.f1.accept(this,q);
        Class temp = new Class(ClassName,null);
        table.Classes.put(ClassName,temp);
        return null;
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n, Class q) {
        return n.f0.toString();
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

        if(table.get_class(ClassName) != null) {
            throw new Exception("Class '" + ClassName + "' already exists!");
        }
        Class temp = new Class(ClassName,null);
        this.table.Classes.put(ClassName,temp);
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
        String extend = n.f3.accept(this,q);

        if(table.get_class(ClassName) != null) {
            throw new Exception("Class '" + ClassName + "' already exists!");
        }
        if(table.get_class(extend) == null) {
            throw new Exception("Class '" + extend + "' hasn't been declared!");
        }

        Class temp = new Class(ClassName,extend);
        this.table.Classes.put(ClassName,temp);
        n.f5.accept(this,temp);
        n.f6.accept(this,temp);
        
        return null;
    }
}
