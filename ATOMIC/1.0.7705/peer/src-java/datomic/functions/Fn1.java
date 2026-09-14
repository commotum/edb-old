package datomic.functions;

/** Invocation contract for a database function with one argument. */
public interface Fn1 {
    /**
     * Invokes the function with its declared argument.
     *
     * @param arg0 first argument
     * @return the function result
     */
    public Object invoke(Object arg0);
}
