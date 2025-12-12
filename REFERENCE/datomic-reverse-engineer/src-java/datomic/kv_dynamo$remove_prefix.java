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

public final class kv_dynamo$remove_prefix
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"blank?");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"replace");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"format");

    public static Object invokeStatic(Object prefix, Object k) {
        Object object;
        Object object2 = k;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__0.getRawRoot()).invoke(prefix);
            if (object3 != null && object3 != Boolean.FALSE) {
                object = k;
                k = null;
            } else {
                Object object4 = k;
                k = null;
                Object object5 = prefix;
                prefix = null;
                object = ((IFn)const__1.getRawRoot()).invoke(object4, ((IFn)const__2.getRawRoot()).invoke((Object)"%s/", object5), (Object)"");
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return kv_dynamo$remove_prefix.invokeStatic(object3, object4);
    }
}

