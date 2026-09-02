package datomic.functions;

/** Invocation contract for a database function with no arguments. */
public interface Fn0 {
    /**
     * Invokes the function.
     *
     * @return the function result
     */
    public Object invoke();
}
