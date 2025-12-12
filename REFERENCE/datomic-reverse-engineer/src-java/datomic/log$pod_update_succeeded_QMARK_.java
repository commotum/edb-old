/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class log$pod_update_succeeded_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"every?");
    public static final AFn const__3 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"rev"), (Object)RT.keyword(null, (String)"etag"));

    public static Object invokeStatic(Object response) {
        Object object = response;
        response = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$pod_update_succeeded_QMARK_.invokeStatic(object2);
    }
}

