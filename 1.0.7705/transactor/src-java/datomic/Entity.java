package datomic;

import datomic.Database;
import java.util.Set;

/**
 * A lazy, associative view of the datoms sharing an entity id.
 *
 * <p>Attribute values are loaded from the database when {@link #get(Object)}
 * or {@link #touch()} requests them and are then cached in the entity. Two
 * entities are equal when their ids match and their database ids match.</p>
 */
public interface Entity {
    /**
     * Returns the value of an attribute.
     *
     * <p>Cardinality-many attributes always return a collection, including
     * when the collection contains a single value.</p>
     *
     * @param key colon-prefixed attribute name, such as
     *            {@code ":user/firstName"}
     * @return the attribute value or values, or {@code null} when absent
     */
    public Object get(Object key);

    /**
     * Loads all attributes and recursively loads component entities.
     *
     * @return this entity
     */
    public Entity touch();

    /**
     * Returns the attribute names available on this entity.
     *
     * @return the available attribute names
     */
    public Set<String> keySet();

    /**
     * Returns the immutable database value underlying this entity.
     *
     * @return the underlying database value
     */
    public Database db();
}
