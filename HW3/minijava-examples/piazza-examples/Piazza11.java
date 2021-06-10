class Test{
    public static void main(String []args){
    System.out.println(100);
    }
}

class A extends C{
    int i;
}

class C extends B{
    int y;
}

class B extends A{
    int z;
}