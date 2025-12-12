/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.algo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.TreeMap;

public final class hash$consistent_ring$fn__19401
extends AFunction {
    int nmembers;
    Object key_hashes_fn;
    Object tm;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"distinct");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vals");

    public hash$consistent_ring$fn__19401(int n, Object object, Object object2) {
        this.nmembers = n;
        this.key_hashes_fn = object;
        this.tm = object2;
    }

    public Object invoke(Object k) {
        Object vec__19402;
        Object object = k;
        k = null;
        Object object2 = vec__19402 = ((IFn)this_.key_hashes_fn).invoke(object);
        vec__19402 = null;
        Object h = RT.nth((Object)object2, (int)RT.intCast((long)0L), null);
        Object object3 = ((IFn)const__5.getRawRoot()).invoke(((TreeMap)this_.tm).tailMap(h));
        Object object4 = h;
        h = null;
        hash$consistent_ring$fn__19401 this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)this_.nmembers, ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object3, ((IFn)const__5.getRawRoot()).invoke(((TreeMap)this_.tm).headMap(object4)))));
    }
}

