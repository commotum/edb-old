package datomic.functions;

/** Invocation contract for a database function with three arguments. */
public interface Fn3 {
    /**
     * Invokes the function with its declared arguments.
     *
     * @param arg0 first argument
     * @param arg1 second argument
     * @param arg2 third argument
     * @return the function result
     */
    public Object invoke(Object arg0, Object arg1, Object arg2);
}
