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

public final class config$fn__820
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"file");

    public static Object invokeStatic(Object props) {
        Object object = props;
        props = null;
        return ((IFn)const__0.getRawRoot()).invoke(RT.get((Object)object, (Object)"datomic.dataDir"), (Object)"indexer");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__820.invokeStatic(object2);
    }
}

