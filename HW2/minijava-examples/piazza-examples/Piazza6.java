class ABitMoreComplex {
  public static void main(String[] args) { }
}

class X {
  int foo;
  boolean bar;
  int some_int;
  X bla;

  public A foo(boolean flag) {
    B b;
    b = new B();
    return b;
  }

  public int some_int() {
    return 10;
  }

  public int[] bar() {
    int[] arr;
    int[] ret;

    int len;

    arr = new int[this.some_int()];

    len = arr.length;
    ret = new int[len * 5];

    return ret;
  }
}

class A {
  int bob;
  boolean bar;
  boolean foo;

  public A foo(int a, A A, int[] c) {
    return this;
  }

  public int baz(boolean flag) {
    return 5;
  }

  public boolean bob() {
    return false;
  }

  public A bar() {
    X x;
    x = new X();
    return this.foo(this.baz(this.bob()), x.foo(true), x.bar());
  }
}

class B extends A {
  public int test() {
    A a;
    X x;
    int[] b;

    x = new X();
    b = new int[5];

    a = this.foo(x.some_int(), this.bar(), b);
    return 6;
  }

  public int baz(boolean flag) {
    return 6;
  }
}