///1234&exitosamente


class Init{
    static void main() {
        var a = new A();
        a.m1();
        a.m2();
    }
}

class A {
    int x;

    void m1() {
        x = 1234;
    }

    void m2() {
        debugPrint(x);
    }
}
