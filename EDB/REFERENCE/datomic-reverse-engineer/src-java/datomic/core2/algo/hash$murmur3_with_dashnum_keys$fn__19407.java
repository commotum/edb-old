/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Murmur3
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.algo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Murmur3;
import clojure.lang.RT;
import clojure.lang.Var;

public final class hash$murmur3_with_dashnum_keys$fn__19407
extends AFunction {
    Object k;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");

    public hash$murmur3_with_dashnum_keys$fn__19407(Object object) {
        this.k = object;
    }

    public Object invoke(Object n) {
        Object object = n;
        n = null;
        hash$murmur3_with_dashnum_keys$fn__19407 this_ = null;
        return Murmur3.hashUnencodedChars((CharSequence)((CharSequence)((IFn)const__0.getRawRoot()).invoke(this_.k, (Object)"-", object)));
    }
}

