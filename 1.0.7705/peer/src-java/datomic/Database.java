package datomic;

import clojure.lang.RT;
import datomic.Attribute;
import datomic.Datom;
import datomic.Entity;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * An immutable, point-in-time value of a database.
 *
 * <p>Database values provide local access to entities, schema, indexes, pull,
 * and database functions. Operations that derive another database value leave
 * this value unchanged.</p>
 */
public interface Database {
    /** Entity-attribute-value-transaction index name. */
    public static final Object EAVT = RT.keyword(null, (String)"eavt");
    /** Attribute-entity-value-transaction index name. */
    public static final Object AEVT = RT.keyword(null, (String)"aevt");
    /** Attribute-value-entity-transaction index name. */
    public static final Object AVET = RT.keyword(null, (String)"avet");
    /** Value-attribute-entity-transaction reverse-reference index name. */
    public static final Object VAET = RT.keyword(null, (String)"vaet");

    /**
     * Returns the opaque, globally unique database id.
     *
     * @return the database id
     */
    public String id();

    /**
     * Returns the t of the most recent transaction included in this value.
     *
     * @return the basis t
     */
    public long basisT();

    /**
     * Returns the next t that would be assigned from this database value.
     *
     * @return the next t
     */
    public long nextT();

    /**
     * Returns the inclusive as-of t, or {@code null} when no as-of bound is
     * present.
     *
     * @return the inclusive as-of t, or {@code null}
     */
    public Long asOfT();

    /**
     * Returns the exclusive since t, or {@code null} when no since bound is
     * present.
     *
     * @return the exclusive since t, or {@code null}
     */
    public Long sinceT();

    /**
     * Returns {@code true} for a database value produced by {@link #history()}.
     *
     * @return whether this is a history database value
     */
    public boolean isHistory();

    /**
     * Applies transaction data locally in memory.
     *
     * <p>The returned report has the same shape as a transaction report. No
     * transaction is submitted to a transactor.</p>
     *
     * @param txData transaction data to apply
     * @return a map containing the database before and after, produced datoms,
     *         and temporary-id resolutions
     */
    public Map with(List txData);

    /**
     * Applies transaction data locally with evaluation options.
     *
     * @param txData transaction data to apply
     * @param options transaction evaluation options
     * @return a transaction report map
     */
    public Map with(List txData, Object options);

    /**
     * Returns a database value containing data through {@code timePoint},
     * inclusive.
     *
     * @param timePoint a transaction id, t, or date
     * @return a database value bounded by {@code timePoint}
     */
    // ATOMIC-NOTE [observed]: Db.asOf changes its temporal bound, not basisT or
    // nextT. The original captured value and its persistent index layers remain.
    public Database asOf(Object timePoint);

    /**
     * Returns a database value containing data after {@code timePoint},
     * exclusive.
     *
     * @param timePoint a transaction id, t, or date
     * @return a database value containing data after {@code timePoint}
     */
    public Database since(Object timePoint);

    /**
     * Returns a history value containing assertions and retractions across
     * time.
     *
     * <p>History values support index access, queries, as-of, and since.
     * Datoms from a history value use {@link Datom#added()} to distinguish
     * assertions from retractions. Entity lookup and local transaction
     * application require a single point-in-time value.</p>
     *
     * @return the history database value
     */
    public Database history();

    /**
     * Returns a database value containing datoms accepted by {@code predicate}.
     *
     * <p>The predicate receives the unfiltered database and each datom.
     * Repeated filtering composes predicates with logical conjunction.</p>
     *
     * @param predicate predicate applied to datoms
     * @return the filtered database value
     */
    public Database filter(Predicate<Datom> predicate);

    /**
     * Returns a filtered database value using a supported callable predicate.
     *
     * @param predicate a database predicate or Clojure function
     * @return the filtered database value
     */
    public Database filter(Object predicate);

    /**
     * Returns {@code true} when this database value has a filter.
     *
     * @return whether this database value is filtered
     */
    public boolean isFiltered();

    /**
     * Returns a lazy associative view of the datoms sharing an entity id.
     *
     * @param entityId an entity id, ident, or lookup reference
     * @return a lazy entity view
     */
    public Entity entity(Object entityId);

    /**
     * Returns schema information for an attribute.
     *
     * @param attributeId an entity identifier naming an attribute
     * @return the attribute schema information
     */
    public Attribute attribute(Object attributeId);

    /**
     * Returns the keyword associated with an entity id. A keyword argument is
     * returned unchanged.
     *
     * @param idOrKeyword an entity id or keyword
     * @return the keyword, or {@code null} when no ident is found
     */
    public Object ident(Object idOrKeyword);

    /**
     * Resolves an entity identifier to an entity id.
     *
     * @param entityId an entity id, ident, or lookup reference
     * @return the entity id, or {@code null} when no entity is found
     */
    public Object entid(Object entityId);

    /**
     * Fabricates an entity id in {@code partition} whose t component is at or
     * after {@code timePoint}.
     *
     * <p>The result can serve as an EAVT seek component for time-based scans of
     * newly created entities.</p>
     *
     * @param partition an entity identifier naming a partition
     * @param timePoint a transaction id, t, or date
     * @return an entity id at or after {@code timePoint}
     */
    public Object entidAt(Object partition, Object timePoint);

    /**
     * Looks up and invokes the database function stored at {@code entityId}.
     * Database functions accept at most ten arguments.
     *
     * @param entityId an entity identifier naming a database function
     * @param args arguments supplied to the function
     * @return the function's result
     */
    public Object invoke(Object entityId, Object ... args);

    /**
     * Returns datoms whose leading index components exactly match the supplied
     * components.
     *
     * <p>EAVT and AEVT contain every datom. AVET contains datoms for indexed or
     * unique attributes. VAET contains reverse references for ref-valued
     * attributes.</p>
     *
     * @param index one of {@link #EAVT}, {@link #AEVT}, {@link #AVET}, or
     *              {@link #VAET}
     * @param components zero or more leading components in index order
     * @return matching datoms in index order
     */
    public Iterable<Datom> datoms(Object index, Object ... components);

    /**
     * Iterates forward from the nearest datom at or after the supplied index
     * components through the end of the index.
     *
     * @param index one of {@link #EAVT}, {@link #AEVT}, {@link #AVET}, or
     *              {@link #VAET}
     * @param components search position in index order
     * @return datoms in forward index order
     */
    public Iterable<Datom> seekDatoms(Object index, Object ... components);

    /**
     * Iterates backward from the nearest datom at or before the supplied index
     * components through the beginning of the index.
     *
     * @param index one of {@link #EAVT}, {@link #AEVT}, {@link #AVET}, or
     *              {@link #VAET}
     * @param components search position in index order
     * @return datoms in reverse index order
     */
    public Iterable<Datom> rseekDatoms(Object index, Object ... components);

    /**
     * Returns AVET datoms for an attribute between {@code start}, inclusive,
     * and {@code end}, exclusive.
     *
     * @param attributeId entity identifier naming an indexed attribute
     * @param start lower value bound, or {@code null} for the beginning
     * @param end upper value bound, or {@code null} for the end
     * @return datoms in the requested value range
     */
    public Iterable<Datom> indexRange(Object attributeId, Object start, Object end);

    /**
     * Returns a hierarchical selection of attributes for an entity.
     *
     * @param pattern a pull pattern or an EDN string containing a pattern
     * @param entityId an entity identifier
     * @return the selected attributes
     */
    public Map pull(Object pattern, Object entityId);

    /**
     * Returns a hierarchical selection of attributes using pull options.
     *
     * @param pattern a pull pattern or an EDN string containing a pattern
     * @param entityId an entity identifier
     * @param options pull evaluation options
     * @return the pull result produced with {@code options}
     */
    public Object pull(Object pattern, Object entityId, Object options);

    /**
     * Walks AVET or AEVT and pulls an entity for each matching datom.
     *
     * <p>The options map contains {@code :index}, {@code :selector}, and
     * {@code :start}; {@code :reverse} optionally selects reverse iteration.
     * The start vector must include at least the attribute, and iteration is
     * limited to datoms for that attribute.</p>
     *
     * @param options index, selector, starting components, and direction
     * @return a stream of pull results
     */
    public Stream<Object> indexPull(Object options);

    /**
     * Returns one hierarchical selection for each supplied entity identifier.
     *
     * @param pattern a pull pattern or an EDN string containing a pattern
     * @param entityIds entity identifiers to pull
     * @return pull results in entity-id order
     */
    public List<Map> pullMany(Object pattern, List entityIds);

    /**
     * Returns hierarchical selections for multiple entities using pull
     * options.
     *
     * @param pattern a pull pattern or an EDN string containing a pattern
     * @param entityIds entity identifiers to pull
     * @param options pull evaluation options
     * @return the pull results produced with {@code options}
     */
    public Object pullMany(Object pattern, List entityIds, Object options);

    /**
     * Returns database statistics, including {@code :datoms}, the total number
     * of datoms in the history database.
     *
     * @return database statistics
     */
    public Map dbStats();

    /**
     * Boolean-valued function used to filter a database.
     *
     * @param <T> value presented to the predicate
     */
    public static interface Predicate<T> {
        /**
         * Tests a value against the unfiltered database.
         *
         * @param db unfiltered database value
         * @param value value to test
         * @return {@code true} when the value belongs in the filtered database
         */
        public boolean apply(Database db, T value);
    }
}
