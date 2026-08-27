/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960$fn__17961$fn__17962
extends AFunction {
    int size__6024__auto__;
    Object db;
    Object b__17957;
    Object c__6023__auto__;
    Object index;
    Object with_key_summary;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"chunk-append");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not=");
    public static final Keyword const__5 = RT.keyword(null, (String)"fulltext");
    public static final Var const__6 = RT.var((String)"datomic.stats", (String)"key-summary");
    public static final AFn const__9 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"aevt"), RT.keyword(null, (String)"avet")});
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"merge-with");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"ns-resolve");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"datomic.stats");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"name");
    public static final Keyword const__19 = RT.keyword(null, (String)"index");
    public static final Keyword const__20 = RT.keyword(null, (String)"tier");

    public stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960$fn__17961$fn__17962(int n, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.size__6024__auto__ = n;
        this.db = object;
        this.b__17957 = object2;
        this.c__6023__auto__ = object3;
        this.index = object4;
        this.with_key_summary = object5;
    }

    public Object invoke() {
        for (long i__17956 = (long)RT.intCast((long)0L); i__17956 < (long)this.size__6024__auto__; ++i__17956) {
            Object object;
            Object and__5236__auto__17964;
            Object tier = ((Indexed)this.c__6023__auto__).nth(RT.intCast((long)i__17956));
            IFn iFn = (IFn)const__3.getRawRoot();
            Object object2 = and__5236__auto__17964 = this.with_key_summary;
            if (object2 != null && object2 != Boolean.FALSE) {
                object = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, this.index);
            } else {
                object = and__5236__auto__17964;
                and__5236__auto__17964 = null;
            }
            Object ks = object != null && object != Boolean.FALSE ? ((IFn)const__6.getRawRoot()).invoke(((IFn)this.index).invoke(((IFn)tier).invoke(this.db)), ((IFn)const__9).invoke(this.index)) : null;
            Object object3 = ((IFn)const__11.getRawRoot()).invoke(const__12.getRawRoot(), const__13.getRawRoot(), ((IFn)const__14.getRawRoot()).invoke(((IFn)((IFn)const__15.getRawRoot()).invoke((Object)const__16, ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke(this.index)))).invoke(this.db, ((IFn)tier).invoke(this.db))));
            Object[] objectArray = new Object[4];
            objectArray[0] = const__19;
            objectArray[1] = this.index;
            objectArray[2] = const__20;
            Object object4 = tier;
            tier = null;
            objectArray[3] = object4;
            Object object5 = ks;
            ks = null;
            iFn.invoke(this.b__17957, ((IFn)const__10.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray), object5));
        }
        return Boolean.TRUE;
    }
}

