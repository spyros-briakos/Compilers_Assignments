class AndShortC {
 public static void main(String[] a) {
      boolean b;
    int x;
        A c;
  

      c = new A();
      b = false;

        if (b && (c.eval()))
          x = 0;
        else
          x = 1;

      System.out.println(x);
}
}

class A {
  public boolean eval(){
    System.out.println(6969);
    return  true;
  }
}
