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
import datomic.fulltext.ClusteredFulltext;

public final class fulltext$clustered_fulltext
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"getx");

    public static Object invokeStatic(Object olookup, Object rootid) {
        ClusteredFulltext clusteredFulltext;
        Object object = rootid;
        if (object != null && object != Boolean.FALSE) {
            olookup = null;
            rootid = null;
            clusteredFulltext = new ClusteredFulltext(olookup, ((IFn)const__0.getRawRoot()).invoke(olookup, rootid));
        } else {
            clusteredFulltext = null;
        }
        return clusteredFulltext;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fulltext$clustered_fulltext.invokeStatic(object3, object4);
    }
}

