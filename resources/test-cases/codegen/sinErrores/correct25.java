///1&2&exitosamente


class Init {
    static void main() {
        var x = new Init();
        x.m1();
        debugPrint(x.m2());
        debugPrint(x.m2(2));
    }

    void m1() {
        return;
    }

    int m2() {
        return 1;
    }

    int m2(int i) {
        return i;
    }
}
