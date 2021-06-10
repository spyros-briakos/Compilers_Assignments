class Main {
    public static void main(String[] arg) {
        A a;
        a = new A();
        System.out.println(((new A()).getA()).foo());
        }
    }
    
    class A{
        public int foo() {
            return 1;
        }
        public A getA() {
            return new A();
        }
    }
