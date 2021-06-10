class to_3eskisma_tou_shta {
    public static void main(String[] a) {}
  }
  
  // The parameter types don't match.
  class A {
    public C foo(C a) { return new C(); }
  }
  
  class B extends A {
    public A foo(B a) { return ((new C())); }
  }
  
  class C extends B {
    public A foo(A a) { return (new B()); }
  }