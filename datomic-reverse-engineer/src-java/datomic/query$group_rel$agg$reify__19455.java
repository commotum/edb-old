/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$group_rel$agg$reify$reify__19456;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

public final class query$group_rel$agg$reify__19455
implements Collection,
RandomAccess,
List,
IObj {
    final IPersistentMap __meta;
    Object cnt;
    Object i;
    Object srel;
    Object r;
    public static final Object const__2 = 0L;
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"atom");
    public static final AFn const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 648, RT.keyword(null, (String)"column"), 38});

    public query$group_rel$agg$reify__19455(IPersistentMap iPersistentMap, Object object, Object object2, Object object3, Object object4) {
        this.__meta = iPersistentMap;
        this.cnt = object;
        this.i = object2;
        this.srel = object3;
        this.r = object4;
    }

    public query$group_rel$agg$reify__19455(Object object, Object object2, Object object3, Object object4) {
        this(null, object, object2, object3, object4);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new query$group_rel$agg$reify__19455(iPersistentMap, this.cnt, this.i, this.srel, this.r);
    }

    public Object get(int off) {
        query$group_rel$agg$reify__19455 this_ = null;
        return RT.nth(((ArrayList)this_.srel).get(RT.intCast((Object)Numbers.add((Object)this_.r, (long)off))), (int)RT.intCast((Object)((Number)this_.i)));
    }

    public Iterator iterator() {
        Object off;
        Object object = off = ((IFn)const__7.getRawRoot()).invoke(const__2);
        off = null;
        return (Iterator)((IObj)new query$group_rel$agg$reify$reify__19456(null, object, this.cnt, this.i, this.srel, this.r)).withMeta((IPersistentMap)const__12);
    }

    public Object[] toArray() {
        Object[] arr = RT.object_array((Object)this.cnt);
        long n__5742__auto__19459 = RT.longCast((Object)this.cnt);
        for (long i = 0L; i < n__5742__auto__19459; ++i) {
            RT.aset((Object[])arr, (int)RT.intCast((long)i), ((List)this).get(RT.intCast((long)i)));
        }
        Object var1_1 = null;
        return arr;
    }

    public int size() {
        return ((Number)this.cnt).intValue();
    }

    public boolean isEmpty() {
        return Boolean.FALSE;
    }
}

