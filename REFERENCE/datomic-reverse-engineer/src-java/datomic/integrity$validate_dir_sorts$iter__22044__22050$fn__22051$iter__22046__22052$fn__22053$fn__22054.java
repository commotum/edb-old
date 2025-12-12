/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052$fn__22053$fn__22054
extends AFunction {
    Object tier;
    Object c__6023__auto__;
    int size__6024__auto__;
    Object b__22049;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"chunk-append");

    public integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052$fn__22053$fn__22054(Object object, Object object2, int n, Object object3) {
        this.tier = object;
        this.c__6023__auto__ = object2;
        this.size__6024__auto__ = n;
        this.b__22049 = object3;
    }

    public Object invoke() {
        for (long i__22048 = (long)RT.intCast((long)0L); i__22048 < (long)this.size__6024__auto__; ++i__22048) {
            Object sort;
            Object object = sort = ((Indexed)this.c__6023__auto__).nth(RT.intCast((long)i__22048));
            sort = null;
            ((IFn)const__3.getRawRoot()).invoke(this.b__22049, (Object)Tuple.create((Object)this.tier, (Object)object));
        }
        return Boolean.TRUE;
    }
}

