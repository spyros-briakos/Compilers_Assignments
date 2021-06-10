class Shadow {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class A {
    int i;

    public int foo(){
        return i;
    }
}

class B extends A {
    boolean i;
   
    public boolean bar() {
        return i;
    }
}