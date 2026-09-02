package datomic.functions;

import java.util.List;

/**
 * Definition of a function stored as a database value.
 *
 * <p>A function object also implements the {@code FnN} interface matching the
 * number of declared parameters, from {@link Fn0} through {@link Fn10}. The
 * function compiles itself when first invoked and reuses the compiled form for
 * subsequent calls.</p>
 *
 * <p>When used as a transaction function, the first parameter receives the
 * database value before the transaction, remaining parameters receive the
 * transaction arguments, and the result must be valid transaction data.
 * Transaction functions are pure and should perform only the work that needs
 * access to the in-transaction database value.</p>
 */
public interface Fn {
    /**
     * Returns the implementation language, such as {@code clojure} or {@code java}.
     *
     * @return the implementation language
     */
    public String lang();

    /**
     * Returns the declared parameter names in invocation order.
     *
     * @return the declared parameter names
     */
    public List<String> params();

    /**
     * Returns the function body source.
     *
     * @return the function body source
     */
    public String code();
}
