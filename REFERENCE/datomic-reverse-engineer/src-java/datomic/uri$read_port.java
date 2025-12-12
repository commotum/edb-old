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

public final class uri$read_port
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"integer?");

    public static Object invokeStatic(Object portstr) {
        Object object;
        Object object2 = portstr;
        portstr = null;
        Object port = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(port);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = port;
            port = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$read_port.invokeStatic(object2);
    }
}

