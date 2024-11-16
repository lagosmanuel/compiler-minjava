///1234&exitosamente


class Init{
    static void main() {
        m1().m2();
    }

    static A m1() {
        return new A(1234);
    }
}

class A {
    int x;

    public A(int x) {
        this.x = x;
    }

    void m2() {
        debugPrint(x);
    }
}
