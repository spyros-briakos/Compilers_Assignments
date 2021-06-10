class Main {
    public static void main(String[] arg) {
        A a;
        a = new A();

        System.out.println(a.foo());
    }
}

class A{
    public int foo() 
    {
        return 1;  
    }
}