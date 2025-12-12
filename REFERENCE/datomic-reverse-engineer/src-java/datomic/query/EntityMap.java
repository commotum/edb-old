/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.Associative
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
 *  clojure.lang.IMapEntry
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentCollection
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Indexed
 *  clojure.lang.MapEntry
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Seqable
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.Associative;
import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IMapEntry;
import clojure.lang.IObj;
import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Indexed;
import clojure.lang.MapEntry;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Seqable;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.Entity;
import datomic.db.Attribute;
import datomic.db.IDbImpl;
import datomic.query.EMapImpl;
import datomic.query.EntityMap$fn__19188;
import datomic.query.EntityMap$fn__19194;
import java.util.Set;

public final class EntityMap
implements Associative,
EMapImpl,
Entity,
ILookup,
IPersistentCollection,
Seqable,
IType {
    public final Object db;
    public final Object eid;
    Object cache;
    public final Object edits;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*print-length*");
    public static final Object const__3 = 20L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"*print-level*");
    public static final Object const__5 = 3L;
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"hash");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__14 = RT.var((String)"datomic.query", (String)"emap");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__18 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__20 = RT.var((String)"datomic.query", (String)"touch");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__32 = RT.var((String)"datomic.query", (String)"get-lazy-entity");
    public static final Var const__33 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__34 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__35 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__37 = RT.var((String)"datomic.db", (String)"normalize-kw");
    public static final Var const__39 = RT.var((String)"datomic.query", (String)"eav");
    public static final Var const__41 = RT.var((String)"clojure.core", (String)"assoc");

    public EntityMap(Object object, Object object2, Object object3, Object object4) {
        this.db = object;
        this.eid = object2;
        this.cache = object3;
        this.edits = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDb")})), (Object)Symbol.intern(null, (String)"eid"), (Object)((IObj)Symbol.intern(null, (String)"cache")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), (Object)Symbol.intern(null, (String)"edits"));
    }

    public Object valAt(Object k, Object not_found) {
        Object object;
        Object object2 = k;
        k = null;
        Object k2 = ((IFn)const__37.getRawRoot()).invoke(object2);
        Object v = RT.get((Object)this.edits, (Object)k2, (Object)this);
        if (Util.identical((Object)v, (Object)this)) {
            Object v2 = RT.get((Object)this.cache, (Object)k2, (Object)this);
            if (Util.identical((Object)v2, (Object)this)) {
                Object v3 = ((IFn)const__39.getRawRoot()).invoke(this.db, this.eid, k2);
                if (Util.identical((Object)v3, null)) {
                    object = not_found;
                    not_found = null;
                } else {
                    Object object3 = k2;
                    k2 = null;
                    this.cache = ((IFn)const__41.getRawRoot()).invoke(this.cache, object3, v3);
                    object = v3;
                    v3 = null;
                }
            } else {
                object = v2;
                v2 = null;
            }
        } else {
            object = v;
            v = null;
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }

    public IMapEntry entryAt(Object k) {
        MapEntry mapEntry;
        Object v = ((ILookup)this).valAt(k, (Object)this);
        if (Util.identical((Object)v, (Object)this)) {
            mapEntry = null;
        } else {
            k = null;
            v = null;
            mapEntry = new MapEntry(((IFn)const__37.getRawRoot()).invoke(k), v);
        }
        return (IMapEntry)mapEntry;
    }

    public boolean containsKey(Object k) {
        Object object = k;
        k = null;
        Object v = ((ILookup)this_).valAt(object, (Object)this_);
        Object object2 = v;
        v = null;
        Boolean bl = Util.identical((Object)object2, (Object)this_) ? Boolean.TRUE : Boolean.FALSE;
        EntityMap this_ = null;
        return (Boolean)((IFn)const__35.getRawRoot()).invoke((Object)bl);
    }

    public ISeq seq() {
        Object m;
        Object object = m = ((IFn)const__32.getRawRoot()).invoke(this_.db, this_.eid);
        m = null;
        EntityMap this_ = null;
        return (ISeq)((IFn)const__13.getRawRoot()).invoke(((IFn)const__33.getRawRoot()).invoke(((IFn)const__34.getRawRoot()).invoke((Object)new EntityMap$fn__19188(this_.edits), object), ((IFn)const__34.getRawRoot()).invoke((Object)new EntityMap$fn__19194(), this_.edits)));
    }

    public Database db() {
        return (Database)this.db;
    }

    public Set keySet() {
        Object object = ((IFn)const__29.getRawRoot()).invoke(((IFn)const__30.getRawRoot()).invoke(const__31.getRawRoot(), const__26.getRawRoot()), ((IFn)const__13.getRawRoot()).invoke((Object)this_));
        EntityMap this_ = null;
        return (Set)((IFn)const__28.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, object);
    }

    public Entity touch() {
        Object seq_19175 = ((IFn)const__13.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke((Object)this));
        Object chunk_19176 = null;
        long count_19177 = 0L;
        long i_19178 = 0L;
        while (true) {
            Object object;
            Object and__5236__auto__19204;
            Object attrid;
            Object temp__5457__auto__19207;
            Object v;
            if (i_19178 < count_19177) {
                Object object2;
                Object and__5236__auto__19200;
                Object attrid2;
                Object a = ((Indexed)chunk_19176).nth(RT.intCast((long)i_19178));
                Object object3 = attrid2 = ((IFn)const__18.getRawRoot()).invoke(this.db, a);
                attrid2 = null;
                Object attr = ((IDbImpl)this.db).elementAt(object3);
                Object object4 = a;
                a = null;
                Object v2 = ((ILookup)this).valAt(object4);
                Object object5 = and__5236__auto__19200 = ((Attribute)attr).isComponent;
                if (object5 != null && object5 != Boolean.FALSE) {
                    object2 = Util.equiv((Object)((Attribute)attr).vtypeid, (long)20L) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    object2 = and__5236__auto__19200;
                    and__5236__auto__19200 = null;
                }
                if (object2 != null && object2 != Boolean.FALSE) {
                    Object object6 = attr;
                    attr = null;
                    if (Util.equiv((long)36L, (Object)((Attribute)object6).cardinality)) {
                        Object object7 = v2;
                        v2 = null;
                        Object seq_19179 = ((IFn)const__13.getRawRoot()).invoke(object7);
                        Object chunk_19180 = null;
                        long count_19181 = 0L;
                        long i_19182 = 0L;
                        while (true) {
                            Object temp__5457__auto__19202;
                            if (i_19182 < count_19181) {
                                Object v3;
                                Object object8 = v3 = ((Indexed)chunk_19180).nth(RT.intCast((long)i_19182));
                                v3 = null;
                                ((IFn)const__20.getRawRoot()).invoke(object8);
                                Object object9 = seq_19179;
                                seq_19179 = null;
                                Object object10 = chunk_19180;
                                chunk_19180 = null;
                                ++i_19182;
                                chunk_19180 = object10;
                                seq_19179 = object9;
                                continue;
                            }
                            Object object11 = seq_19179;
                            seq_19179 = null;
                            Object object12 = temp__5457__auto__19202 = ((IFn)const__13.getRawRoot()).invoke(object11);
                            if (object12 == null || object12 == Boolean.FALSE) break;
                            Object object13 = temp__5457__auto__19202;
                            temp__5457__auto__19202 = null;
                            Object seq_191792 = object13;
                            Object object14 = ((IFn)const__22.getRawRoot()).invoke(seq_191792);
                            if (object14 != null && object14 != Boolean.FALSE) {
                                Object c__5719__auto__19201 = ((IFn)const__23.getRawRoot()).invoke(seq_191792);
                                Object object15 = seq_191792;
                                seq_191792 = null;
                                Object object16 = c__5719__auto__19201;
                                Object object17 = c__5719__auto__19201;
                                c__5719__auto__19201 = null;
                                i_19182 = RT.intCast((long)0L);
                                count_19181 = RT.intCast((int)RT.count((Object)object17));
                                chunk_19180 = object16;
                                seq_19179 = ((IFn)const__24.getRawRoot()).invoke(object15);
                                continue;
                            }
                            Object object18 = v = ((IFn)const__26.getRawRoot()).invoke(seq_191792);
                            v = null;
                            ((IFn)const__20.getRawRoot()).invoke(object18);
                            Object object19 = seq_191792;
                            seq_191792 = null;
                            i_19182 = 0L;
                            count_19181 = 0L;
                            chunk_19180 = null;
                            seq_19179 = ((IFn)const__27.getRawRoot()).invoke(object19);
                        }
                    } else {
                        Object object20 = v2;
                        v2 = null;
                        ((IFn)const__20.getRawRoot()).invoke(object20);
                    }
                }
                Object object21 = seq_19175;
                seq_19175 = null;
                Object object22 = chunk_19176;
                chunk_19176 = null;
                ++i_19178;
                chunk_19176 = object22;
                seq_19175 = object21;
                continue;
            }
            Object object23 = seq_19175;
            seq_19175 = null;
            Object object24 = temp__5457__auto__19207 = ((IFn)const__13.getRawRoot()).invoke(object23);
            if (object24 == null || object24 == Boolean.FALSE) break;
            Object object25 = temp__5457__auto__19207;
            temp__5457__auto__19207 = null;
            Object seq_191752 = object25;
            Object object26 = ((IFn)const__22.getRawRoot()).invoke(seq_191752);
            if (object26 != null && object26 != Boolean.FALSE) {
                Object c__5719__auto__19203 = ((IFn)const__23.getRawRoot()).invoke(seq_191752);
                Object object27 = seq_191752;
                seq_191752 = null;
                Object object28 = c__5719__auto__19203;
                Object object29 = c__5719__auto__19203;
                c__5719__auto__19203 = null;
                i_19178 = RT.intCast((long)0L);
                count_19177 = RT.intCast((int)RT.count((Object)object29));
                chunk_19176 = object28;
                seq_19175 = ((IFn)const__24.getRawRoot()).invoke(object27);
                continue;
            }
            Object a = ((IFn)const__26.getRawRoot()).invoke(seq_191752);
            Object object30 = attrid = ((IFn)const__18.getRawRoot()).invoke(this.db, a);
            attrid = null;
            Object attr = ((IDbImpl)this.db).elementAt(object30);
            Object object31 = a;
            a = null;
            Object v4 = ((ILookup)this).valAt(object31);
            Object object32 = and__5236__auto__19204 = ((Attribute)attr).isComponent;
            if (object32 != null && object32 != Boolean.FALSE) {
                object = Util.equiv((Object)((Attribute)attr).vtypeid, (long)20L) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object = and__5236__auto__19204;
                and__5236__auto__19204 = null;
            }
            if (object != null && object != Boolean.FALSE) {
                Object object33 = attr;
                attr = null;
                if (Util.equiv((long)36L, (Object)((Attribute)object33).cardinality)) {
                    Object object34 = v4;
                    v4 = null;
                    Object seq_19183 = ((IFn)const__13.getRawRoot()).invoke(object34);
                    Object chunk_19184 = null;
                    long count_19185 = 0L;
                    long i_19186 = 0L;
                    while (true) {
                        Object v5;
                        Object temp__5457__auto__19206;
                        if (i_19186 < count_19185) {
                            Object object35 = v = ((Indexed)chunk_19184).nth(RT.intCast((long)i_19186));
                            v = null;
                            ((IFn)const__20.getRawRoot()).invoke(object35);
                            Object object36 = seq_19183;
                            seq_19183 = null;
                            Object object37 = chunk_19184;
                            chunk_19184 = null;
                            ++i_19186;
                            chunk_19184 = object37;
                            seq_19183 = object36;
                            continue;
                        }
                        Object object38 = seq_19183;
                        seq_19183 = null;
                        Object object39 = temp__5457__auto__19206 = ((IFn)const__13.getRawRoot()).invoke(object38);
                        if (object39 == null || object39 == Boolean.FALSE) break;
                        Object object40 = temp__5457__auto__19206;
                        temp__5457__auto__19206 = null;
                        Object seq_191832 = object40;
                        Object object41 = ((IFn)const__22.getRawRoot()).invoke(seq_191832);
                        if (object41 != null && object41 != Boolean.FALSE) {
                            Object c__5719__auto__19205 = ((IFn)const__23.getRawRoot()).invoke(seq_191832);
                            Object object42 = seq_191832;
                            seq_191832 = null;
                            Object object43 = c__5719__auto__19205;
                            Object object44 = c__5719__auto__19205;
                            c__5719__auto__19205 = null;
                            i_19186 = RT.intCast((long)0L);
                            count_19185 = RT.intCast((int)RT.count((Object)object44));
                            chunk_19184 = object43;
                            seq_19183 = ((IFn)const__24.getRawRoot()).invoke(object42);
                            continue;
                        }
                        Object object45 = v5 = ((IFn)const__26.getRawRoot()).invoke(seq_191832);
                        v5 = null;
                        ((IFn)const__20.getRawRoot()).invoke(object45);
                        Object object46 = seq_191832;
                        seq_191832 = null;
                        i_19186 = 0L;
                        count_19185 = 0L;
                        chunk_19184 = null;
                        seq_19183 = ((IFn)const__27.getRawRoot()).invoke(object46);
                    }
                } else {
                    Object object47 = v4;
                    v4 = null;
                    ((IFn)const__20.getRawRoot()).invoke(object47);
                }
            }
            Object object48 = seq_191752;
            seq_191752 = null;
            i_19178 = 0L;
            count_19177 = 0L;
            chunk_19176 = null;
            seq_19175 = ((IFn)const__27.getRawRoot()).invoke(object48);
        }
        return this;
    }

    public Object get(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object);
    }

    public IPersistentCollection empty() {
        EntityMap this_ = null;
        return (IPersistentCollection)((IFn)const__14.getRawRoot()).invoke(this_.db, this_.eid);
    }

    public int count() {
        return RT.count((Object)((IFn)const__13.getRawRoot()).invoke((Object)this));
    }

    public boolean equiv(Object o) {
        EntityMap entityMap = this_;
        Object object = o;
        o = null;
        EntityMap this_ = null;
        return ((Object)entityMap).equals(object);
    }

    public int hashCode() {
        return Util.hashCombine((int)RT.intCast((Object)((Number)((IFn)const__11.getRawRoot()).invoke(((IDbImpl)this.db).getRawId()))), (int)RT.intCast((Object)((Number)((IFn)const__11.getRawRoot()).invoke(this.eid))));
    }

    /*
     * WARNING - void declaration
     */
    public boolean equals(Object other) {
        boolean bl;
        boolean and__5236__auto__19209 = other instanceof EntityMap;
        if (and__5236__auto__19209) {
            boolean and__5236__auto__19208 = Util.equiv((Object)this.eid, (Object)((EntityMap)other).eid);
            if (and__5236__auto__19208) {
                Object object = other;
                other = null;
                bl = Util.equiv((Object)((IDbImpl)this.db).getRawId(), (Object)((IDbImpl)((Object)((Entity)object).db())).getRawId());
            } else {
                void var3_3;
                bl = var3_3;
            }
        } else {
            void var2_2;
            bl = var2_2;
        }
        return bl;
    }

    public String toString() {
        Object object;
        ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__2, const__3, (Object)const__4, const__5));
        try {
            object = ((IFn)const__6.getRawRoot()).invoke((Object)this);
        }
        finally {
            ((IFn)const__7.getRawRoot()).invoke();
        }
        return (String)object;
    }

    public Object cache() {
        return this.cache;
    }
}

