package datomic.functions;

/** Invocation contract for a database function with two arguments. */
public interface Fn2 {
    /**
     * Invokes the function with its declared arguments.
     *
     * @param arg0 first argument
     * @param arg1 second argument
     * @return the function result
     */
    public Object invoke(Object arg0, Object arg1);
}
