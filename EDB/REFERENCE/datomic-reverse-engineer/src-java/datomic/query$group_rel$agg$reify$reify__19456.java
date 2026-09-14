/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.ArrayList;
import java.util.Iterator;

public final class query$group_rel$agg$reify$reify__19456
implements Iterator,
IObj {
    final IPersistentMap __meta;
    Object off;
    Object cnt;
    Object i;
    Object srel;
    Object r;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"inc");

    public query$group_rel$agg$reify$reify__19456(IPersistentMap iPersistentMap, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.__meta = iPersistentMap;
        this.off = object;
        this.cnt = object2;
        this.i = object3;
        this.srel = object4;
        this.r = object5;
    }

    public query$group_rel$agg$reify$reify__19456(Object object, Object object2, Object object3, Object object4, Object object5) {
        this(null, object, object2, object3, object4, object5);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new query$group_rel$agg$reify$reify__19456(iPersistentMap, this.off, this.cnt, this.i, this.srel, this.r);
    }

    public Object next() {
        Object ret = RT.nth(((ArrayList)this.srel).get(RT.intCast((Object)Numbers.add((Object)this.r, (Object)((IFn)const__2.getRawRoot()).invoke(this.off)))), (int)RT.intCast((Object)((Number)this.i)));
        ((IFn)const__5.getRawRoot()).invoke(this.off, const__6.getRawRoot());
        Object var1_1 = null;
        return ret;
    }

    public boolean hasNext() {
        return RT.booleanCast((boolean)Numbers.lt((Object)((IFn)const__2.getRawRoot()).invoke(this.off), (Object)this.cnt));
    }
}

