package datomic;

import clojure.lang.RT;

/**
 * Programmatic representation of a schema attribute.
 *
 * <p>Attribute information is kept in memory, so this interface is the
 * efficient way to inspect schema properties that are needed repeatedly.</p>
 */
// ATOMIC-NOTE BEGIN foundation-schema (baseline cd7192e63d883a4a34aa7de4d5bcd17e6edb692d)
// Observed: db/create-attribute builds the in-memory descriptor from schema
// information. db/Attribute.hasAVET requires storageHasAVET AND needsAVET;
// db/AttrInfo exposes the public projection below. Logical indexing intent and
// physically available index coverage are different state, needed by index reads
// and schema-transition checks; do not infer readiness just from :db/index.
// Rust src/schema.rs derives descriptors from ordinary schema datoms, while
// tiered_assessor.rs checks physical_avet_ready separately. Retain that split.
// A source/docs conflict remains: db/install-attribute-errors rejects unique
// bytes (type 27), as Rust does; the Identity and Uniqueness chapter says any
// value type. This interface does not decide that conflict. See the companion
// datomic_pro_docs/03_schema/03_identity_and_uniqueness.atomic.md for evidence.
// ATOMIC-NOTE END foundation-schema
public interface Attribute {
    /** Cardinality-many schema value. */
    public static final Object CARDINALITY_MANY = RT.keyword((String)"db.cardinality", (String)"many");
    /** Cardinality-one schema value. */
    public static final Object CARDINALITY_ONE = RT.keyword((String)"db.cardinality", (String)"one");
    /** Unique identity schema value. */
    public static final Object UNIQUE_IDENTITY = RT.keyword((String)"db.unique", (String)"identity");
    /** Unique value schema value. */
    public static final Object UNIQUE_VALUE = RT.keyword((String)"db.unique", (String)"value");
    /** Arbitrary-precision decimal value type. */
    public static final Object TYPE_BIGDEC = RT.keyword((String)"db.type", (String)"bigdec");
    /** Arbitrary-precision integer value type. */
    public static final Object TYPE_BIGINT = RT.keyword((String)"db.type", (String)"bigint");
    /** Boolean value type. */
    public static final Object TYPE_BOOLEAN = RT.keyword((String)"db.type", (String)"boolean");
    /** Byte array value type. */
    public static final Object TYPE_BYTES = RT.keyword((String)"db.type", (String)"bytes");
    /** Double-precision floating-point value type. */
    public static final Object TYPE_DOUBLE = RT.keyword((String)"db.type", (String)"double");
    /** Database function value type. */
    public static final Object TYPE_FN = RT.keyword((String)"db.type", (String)"fn");
    /** Single-precision floating-point value type. */
    public static final Object TYPE_FLOAT = RT.keyword((String)"db.type", (String)"float");
    /** Instant value type. */
    public static final Object TYPE_INSTANT = RT.keyword((String)"db.type", (String)"instant");
    /** Keyword value type. */
    public static final Object TYPE_KEYWORD = RT.keyword((String)"db.type", (String)"keyword");
    /** 64-bit integer value type. */
    public static final Object TYPE_LONG = RT.keyword((String)"db.type", (String)"long");
    /** Entity reference value type. */
    public static final Object TYPE_REF = RT.keyword((String)"db.type", (String)"ref");
    /** String value type. */
    public static final Object TYPE_STRING = RT.keyword((String)"db.type", (String)"string");
    /** URI value type. */
    public static final Object TYPE_URI = RT.keyword((String)"db.type", (String)"uri");
    /** UUID value type. */
    public static final Object TYPE_UUID = RT.keyword((String)"db.type", (String)"uuid");

    /**
     * Returns this attribute's entity id.
     *
     * @return the attribute entity id
     */
    public Object id();

    /**
     * Returns this attribute's programmatic name.
     *
     * @return the attribute ident
     */
    public Object ident();

    /**
     * Returns one of the {@code TYPE_*} values describing values accepted by
     * this attribute.
     *
     * @return the attribute value type
     */
    public Object valueType();

    /**
     * Returns {@link #CARDINALITY_ONE} or {@link #CARDINALITY_MANY}.
     *
     * @return the attribute cardinality
     */
    public Object cardinality();

    /**
     * Returns {@link #UNIQUE_IDENTITY}, {@link #UNIQUE_VALUE}, or {@code null}
     * when this attribute has no uniqueness constraint.
     *
     * @return the uniqueness constraint, or {@code null}
     */
    public Object unique();

    /**
     * Returns {@code true} when the attribute identifies component entities.
     *
     * @return whether the attribute identifies component entities
     */
    public boolean isComponent();

    /**
     * Returns {@code true} when the attribute is configured for the AVET
     * index, including attributes that are unique.
     *
     * @return whether AVET indexing is configured
     */
    public boolean isIndexed();

    /**
     * Returns {@code true} when the attribute currently has an AVET index.
     *
     * <p>After indexing is enabled on an existing attribute, this becomes
     * {@code true} when background index creation is complete.</p>
     *
     * @return whether the attribute currently has an AVET index
     */
    public boolean hasAVET();

    /**
     * Returns {@code true} when historical values are not retained for this
     * attribute.
     *
     * @return whether historical values are omitted
     */
    public boolean hasNoHistory();

    /**
     * Returns {@code true} when the attribute has full-text indexing enabled.
     *
     * @return whether full-text indexing is enabled
     */
    public boolean hasFulltext();

    /**
     * Returns {@code true} when maintenance of this composite tuple attribute
     * has been permanently discontinued.
     *
     * @return whether composite tuple maintenance has been discontinued
     */
    public boolean isTupleDiscontinued();
}
