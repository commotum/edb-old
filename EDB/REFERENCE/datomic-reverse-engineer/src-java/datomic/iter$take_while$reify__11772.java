/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.iter.Iter;

public final class iter$take_while$reify__11772
implements Iter,
IObj {
    final IPersistentMap __meta;
    Object p;
    Object iter;
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"take-while");

    public iter$take_while$reify__11772(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.p = object;
        this.iter = object2;
    }

    public iter$take_while$reify__11772(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new iter$take_while$reify__11772(iPersistentMap, this.p, this.iter);
    }

    public Object next() {
        Object object;
        Object object2;
        Object inext2;
        Object and__5236__auto__11774;
        Object object3 = and__5236__auto__11774 = (inext2 = ((Iter)this_.iter).next());
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = ((IFn)this_.p).invoke(((Iter)inext2).get());
        } else {
            object2 = and__5236__auto__11774;
            Object var2_2 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            if (Util.identical((Object)inext2, (Object)this_.iter)) {
                object = this_;
            } else {
                Object object4 = inext2;
                inext2 = null;
                iter$take_while$reify__11772 this_ = null;
                object = ((IFn)const__1.getRawRoot()).invoke(this_.p, object4);
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object get() {
        return ((Iter)this.iter).get();
    }
}

