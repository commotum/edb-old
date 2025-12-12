/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.RT;

public interface Attribute {
    public static final Object CARDINALITY_MANY = RT.keyword((String)"db.cardinality", (String)"many");
    public static final Object CARDINALITY_ONE = RT.keyword((String)"db.cardinality", (String)"one");
    public static final Object UNIQUE_IDENTITY = RT.keyword((String)"db.unique", (String)"identity");
    public static final Object UNIQUE_VALUE = RT.keyword((String)"db.unique", (String)"value");
    public static final Object TYPE_BIGDEC = RT.keyword((String)"db.type", (String)"bigdec");
    public static final Object TYPE_BIGINT = RT.keyword((String)"db.type", (String)"bigint");
    public static final Object TYPE_BOOLEAN = RT.keyword((String)"db.type", (String)"boolean");
    public static final Object TYPE_BYTES = RT.keyword((String)"db.type", (String)"bytes");
    public static final Object TYPE_DOUBLE = RT.keyword((String)"db.type", (String)"double");
    public static final Object TYPE_FN = RT.keyword((String)"db.type", (String)"fn");
    public static final Object TYPE_FLOAT = RT.keyword((String)"db.type", (String)"float");
    public static final Object TYPE_INSTANT = RT.keyword((String)"db.type", (String)"instant");
    public static final Object TYPE_KEYWORD = RT.keyword((String)"db.type", (String)"keyword");
    public static final Object TYPE_LONG = RT.keyword((String)"db.type", (String)"long");
    public static final Object TYPE_REF = RT.keyword((String)"db.type", (String)"ref");
    public static final Object TYPE_STRING = RT.keyword((String)"db.type", (String)"string");
    public static final Object TYPE_URI = RT.keyword((String)"db.type", (String)"uri");
    public static final Object TYPE_UUID = RT.keyword((String)"db.type", (String)"uuid");

    public Object id();

    public Object ident();

    public Object valueType();

    public Object cardinality();

    public Object unique();

    public boolean isComponent();

    public boolean isIndexed();

    public boolean hasAVET();

    public boolean hasNoHistory();

    public boolean hasFulltext();
}

