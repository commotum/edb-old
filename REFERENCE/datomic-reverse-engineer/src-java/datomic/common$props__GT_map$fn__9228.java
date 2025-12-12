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
import java.util.Properties;

public final class common$props__GT_map$fn__9228
extends AFunction {
    Object props;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");

    public common$props__GT_map$fn__9228(Object object) {
        this.props = object;
    }

    public Object invoke(Object m, Object k) {
        Object object = m;
        m = null;
        Object object2 = k;
        Object object3 = k;
        k = null;
        common$props__GT_map$fn__9228 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)((Properties)this_.props).getProperty((String)object3));
    }
}

