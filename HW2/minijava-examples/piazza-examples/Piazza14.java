class Main {
  public static void main(String[] args) {
    X x;
    x = new X();
    System.out.println(x.bar()); // Prints 6
  }
}

class P {
  public int foo() {
    return 5;
  }
}

class L extends P {
  public int foo() {
    return 6;
  }
}

class X extends L {
  public int bar() {
    return this.foo();
  }
}