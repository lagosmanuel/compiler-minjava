///1&exitosamente


class Init {
    static void main() {
        new B();
    }
}

class A {
    int x;
    public A() {
        x = 1;
    }
}

class B extends A {
    boolean x;
    public B() {
        x = false;
        debugPrint(super.x);
    }
}
