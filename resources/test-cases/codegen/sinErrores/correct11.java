///1&2&3&exitosamente


class Init{
    static void main() {
        Lista lista = new Lista();
        for (Integer i:lista) {
            int x;
            int y;
            debugPrint(i);
            if (i == 3) break;
            int z;
        }
    }
}

class Lista extends MiniIterable<Integer> {
    int x;

    public boolean hasNext() {
        return x < 5;
    }

    public void start() {
        x = 0;
    }

    public Integer next() {
        return x+=1;
    }
}
