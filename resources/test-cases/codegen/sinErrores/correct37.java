///1234&exitosamente


class Init{
    static void main() {
        m1().m2();
    }

    static A m1() {
        return new A();
    }
}

class A {
    void m2() {
        debugPrint(1234);
    }
}
