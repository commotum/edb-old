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
// ATOMIC-NOTE [observed]: query/EntityMap implements this contract. valAt reads
// eav/rae lazily; touch caches direct attributes and follows only components.
// eav/ref-val may expose a forward ref's ident keyword; rae instead wraps every
// incoming parent with emap, including identified parents and reverse components.
// equals/hashCode use raw database identity plus eid, not basis or loaded values.
// Native Entity retains an immutable DatabaseValue and synchronized local cache;
// JVM Associative/Seqable/string-key conveniences are not separate native engines.
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
