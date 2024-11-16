///1234&exitosamente


class Init{
    static int x;

    static void main() {
        Init.m1();
        Init.m2();
    }

    static void m1() {
        x = 1234;
    }

    static void m2() {
        debugPrint(x);
    }
}