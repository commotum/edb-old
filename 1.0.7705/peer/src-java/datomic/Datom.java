package datomic;

/**
 * An immutable, point-in-time fact represented as
 * {@code [entity, attribute, value, transaction, added]}.
 */
public interface Datom {
    /**
     * Returns this datom's entity id.
     *
     * @return the entity id
     */
    public Object e();

    /**
     * Returns this datom's attribute id.
     *
     * @return the attribute id
     */
    public Object a();

    /**
     * Returns this datom's value.
     *
     * @return the value
     */
    public Object v();

    /**
     * Returns the transaction id that produced this datom.
     *
     * @return the transaction id
     */
    public Object tx();

    /**
     * Indicates whether this datom is an assertion or retraction.
     *
     * @return {@code true} for an assertion and {@code false} for a retraction
     */
    public boolean added();

    /**
     * Returns a component by tuple position: entity at 0, attribute at 1,
     * value at 2, transaction at 3, and added at 4.
     *
     * @param index tuple position
     * @return the component at {@code index}
     */
    public Object get(int index);
}
