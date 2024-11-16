///A&B&exitosamente


class Init {
    static void main() {
        new B();
    }
}

class A {
    public A() {
        System.printSln("A");
    }
}

class B extends A {
    public B() {
        super();
        System.printSln("B");
    }
}