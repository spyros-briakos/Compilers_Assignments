class Test{
    public static void main(String []args) {
        System.out.println(100);
    }
}


class A{
    int i;
    boolean flag;
    int j;
    A temp;
    public int foo() {
        return 2;
    } 
    public boolean fa() {
        return true;
    }
}

class B extends A{
    A type;
    int k;
    public int foo() {
        return 1;
    }
    public boolean bla() {
        return false;
    }
}