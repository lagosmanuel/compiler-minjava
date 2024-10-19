class HelloWorld {

    public static void main() {
        A.m1(1);
    }
}

class A {
    static B m1() {

    }

    public static void m1(int x) {
        m1().a;
    }
}

class B {
    private int a;

    private void m2() {

    }
}