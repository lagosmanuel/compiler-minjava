///123&exitosamente


class Init{
    int y = 123;
    int z = y;
    int x = z;

    static void main() {
        debugPrint(new Init().m1());
    }

    int m1() {
        return x;
    }
}
