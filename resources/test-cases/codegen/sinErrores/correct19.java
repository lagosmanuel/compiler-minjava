///1&1&exitosamente


class Init {
    static int x;
    static int y;

    static void main() {
        var a = new Init();
        x = 1;
        a.y = a.x;
        debugPrint(y);
        debugPrint(a.y);
    }
}
