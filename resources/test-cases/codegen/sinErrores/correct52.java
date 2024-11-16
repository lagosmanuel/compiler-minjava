///ab&exitosamente


class Init {
    static void main() {
        String s = "ab";

        switch (s) {
            case "a": {
                System.printSln("a");
                break;
            }
            case "ab": {
                System.printSln("ab");
                debugPrint(2);
                break;
            }
            case "abc": {
                System.printSln("abc");
                debugPrint(3);
                break;
            }
            default: {
                System.printSln("default");
                break;
            }
        }
    }
}
