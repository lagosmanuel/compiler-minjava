///[SinErrores]
class A<D> {
    public D m1() {}
}

class B<E> extends A<E> {
    public E m1() {}
}

class C<F> extends B<F>{
    public F m1() {}
}

class Init{
    static void main()
    { }
}
