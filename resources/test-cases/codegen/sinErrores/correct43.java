///1&c&true&exitosamente

class Init{
    static void main() {
        Integer i = m1();
        Character c = m2();
        Boolean b = m3();
        int i2 = i + 1;
        char c2 = c;
        boolean b2 = b && true;
        System.printIln(i2);
        System.printCln(c2);
        System.printBln(b2);
    }

    static Integer m1() {
        return new Integer(0);
    }

    static Character m2() {
        return new Character('c');
    }

    static Boolean m3() {
        return new Boolean(true);
    }
}
