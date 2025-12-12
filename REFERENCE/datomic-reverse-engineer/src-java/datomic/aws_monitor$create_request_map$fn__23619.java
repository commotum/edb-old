/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class aws_monitor$create_request_map$fn__23619
extends AFunction {
    Object dimensions;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__1 = RT.keyword(null, (String)"dimensions");

    public aws_monitor$create_request_map$fn__23619(Object object) {
        this.dimensions = object;
    }

    public Object invoke(Object p1__23618_SHARP_) {
        Object object = p1__23618_SHARP_;
        p1__23618_SHARP_ = null;
        aws_monitor$create_request_map$fn__23619 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, this_.dimensions);
    }
}

