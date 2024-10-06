///[Error:m1|11]
class K {

}

class B<K>{
    K m1() {}
}

class A extends B<K> {
    K m1() {}
}

class Init{
    static void main()
    { }
}
