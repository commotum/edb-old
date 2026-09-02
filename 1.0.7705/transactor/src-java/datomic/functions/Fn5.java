package datomic.functions;

/** Invocation contract for a database function with five arguments. */
public interface Fn5 {
    /**
     * Invokes the function with its declared arguments.
     *
     * @param arg0 first argument
     * @param arg1 second argument
     * @param arg2 third argument
     * @param arg3 fourth argument
     * @param arg4 fifth argument
     * @return the function result
     */
    public Object invoke(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4);
}
