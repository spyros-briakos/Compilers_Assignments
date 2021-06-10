//error (Α not equal to or subtype of B)
class Test {
  public static void main(String[] args) {
    B b;
    b = new A();
  }
}

class A {}
class B extends A {}