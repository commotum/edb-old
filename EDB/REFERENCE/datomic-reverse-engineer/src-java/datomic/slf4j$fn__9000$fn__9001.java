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

public final class slf4j$fn__9000$fn__9001
extends AFunction {
    Object redact_QMARK_;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");

    public slf4j$fn__9000$fn__9001(Object object) {
        this.redact_QMARK_ = object;
    }

    public Object invoke(Object m, Object k, Object v) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = m;
        m = null;
        Object object3 = k;
        Object object4 = k;
        k = null;
        Object object5 = ((IFn)this_.redact_QMARK_).invoke(object4);
        if (object5 != null && object5 != Boolean.FALSE) {
            object = "<redacted>";
        } else {
            object = v;
            v = null;
        }
        slf4j$fn__9000$fn__9001 this_ = null;
        return iFn.invoke(object2, object3, object);
    }
}

