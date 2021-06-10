class TestLength {
  public static void main(String[] args) {
    System.out.println((((new A()).foo()).length) + 1);
  }
}

class A {
  public int[] foo() {
    int[] x;
    x = new int [5];
    return x;
  }
}
