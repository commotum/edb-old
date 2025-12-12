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

public final class iter$filter$reify__11767
implements Iter,
IObj {
    final IPersistentMap __meta;
    Object p;
    Object iter;
    Object advance;
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"filter");

    public iter$filter$reify__11767(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.p = object;
        this.iter = object2;
        this.advance = object3;
    }

    public iter$filter$reify__11767(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new iter$filter$reify__11767(iPersistentMap, this.p, this.iter, this.advance);
    }

    public Object next() {
        Object object;
        Object temp__5457__auto__11769;
        Object object2 = temp__5457__auto__11769 = ((IFn)this_.advance).invoke(((Iter)this_.iter).next());
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5457__auto__11769;
            temp__5457__auto__11769 = null;
            Object inext2 = object3;
            if (Util.identical((Object)inext2, (Object)this_.iter)) {
                object = this_;
            } else {
                Object object4 = inext2;
                inext2 = null;
                iter$filter$reify__11767 this_ = null;
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

