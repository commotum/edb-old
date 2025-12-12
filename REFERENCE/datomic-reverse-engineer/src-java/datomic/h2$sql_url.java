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

public final class h2$sql_url
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object data_dir) {
        Object object = data_dir;
        data_dir = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)"jdbc:h2:", object, (Object)"/datomic");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$sql_url.invokeStatic(object2);
    }
}

