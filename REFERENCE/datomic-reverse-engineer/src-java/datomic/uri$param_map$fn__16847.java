/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.regex.Pattern;

public final class uri$param_map$fn__16847
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"split");
    public static final Object const__1 = Pattern.compile("=");

    public Object invoke(Object p1__16840_SHARP_) {
        Object object = p1__16840_SHARP_;
        p1__16840_SHARP_ = null;
        uri$param_map$fn__16847 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1);
    }
}

