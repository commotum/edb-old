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

public final class kv_dynamo$key_path
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"blank?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"format");

    public static Object invokeStatic(Object prefix, Object k) {
        Object object;
        Object object2 = k;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3;
            IFn iFn = (IFn)const__0.getRawRoot();
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(prefix);
            if (object4 != null && object4 != Boolean.FALSE) {
                object3 = null;
            } else {
                Object object5 = prefix;
                prefix = null;
                object3 = ((IFn)const__2.getRawRoot()).invoke((Object)"%s/", object5);
            }
            Object object6 = k;
            k = null;
            object = iFn.invoke(object3, object6);
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
        return kv_dynamo$key_path.invokeStatic(object3, object4);
    }
}

