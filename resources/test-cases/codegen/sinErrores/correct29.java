///A&exitosamente


class Init {
    static void main() {
        new B();
    }
}

class A {
    void m1() {
        System.printSln("A");
    }
}

class B extends A {
    void m1() {
        System.printSln("B");
    }
    public B() {
        super.m1();
    }
}
