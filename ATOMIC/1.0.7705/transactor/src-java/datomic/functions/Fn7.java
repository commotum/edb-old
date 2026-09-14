package datomic.functions;

/** Invocation contract for a database function with seven arguments. */
public interface Fn7 {
    /**
     * Invokes the function with its declared arguments.
     *
     * @param arg0 first argument
     * @param arg1 second argument
     * @param arg2 third argument
     * @param arg3 fourth argument
     * @param arg4 fifth argument
     * @param arg5 sixth argument
     * @param arg6 seventh argument
     * @return the function result
     */
    public Object invoke(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6);
}
