package datomic;

import clojure.lang.RT;
import java.util.Map;

/**
 * An immutable value providing sequential access to the transaction log.
 */
public interface Log {
    /** Transaction-map key containing the transaction's t value. */
    public static final Object T = RT.keyword(null, (String)"t");
    /** Transaction-map key containing datoms asserted or retracted. */
    public static final Object DATA = RT.keyword(null, (String)"data");

    /**
     * Returns transactions from {@code startT}, inclusive, to {@code endT},
     * exclusive.
     *
     * <p>Each result is a map containing {@link #T} and {@link #DATA}.
     * A {@code null} start begins at the start of the log, and a {@code null}
     * end continues through the end of the log.</p>
     *
     * @param startT time point at which to begin, or {@code null}
     * @param endT time point at which to stop, or {@code null}
     * @return transactions in ascending t order
     */
    public Iterable<Map> txRange(Object startT, Object endT);
}
