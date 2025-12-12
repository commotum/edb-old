/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.common$props__GT_map$fn__9228;

public final class common$props__GT_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keys");

    public static Object invokeStatic(Object props) {
        common$props__GT_map$fn__9228 common$props__GT_map$fn__9228 = new common$props__GT_map$fn__9228(props);
        Object object = props;
        props = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)common$props__GT_map$fn__9228, (Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$props__GT_map.invokeStatic(object2);
    }
}

