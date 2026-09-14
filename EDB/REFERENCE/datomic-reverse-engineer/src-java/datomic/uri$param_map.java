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
import datomic.uri$param_map$fn__16842;
import datomic.uri$param_map$fn__16847;
import java.util.regex.Pattern;

public final class uri$param_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.string", (String)"split");
    public static final Object const__3 = Pattern.compile("&");

    public static Object invokeStatic(Object query2) {
        Object object;
        Object object2 = query2;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3;
            Object or__5238__auto__16850;
            IFn iFn = (IFn)const__0.getRawRoot();
            IFn iFn2 = (IFn)const__1.getRawRoot();
            uri$param_map$fn__16842 uri$param_map$fn__16842 = new uri$param_map$fn__16842();
            IFn iFn3 = (IFn)const__1.getRawRoot();
            uri$param_map$fn__16847 uri$param_map$fn__16847 = new uri$param_map$fn__16847();
            IFn iFn4 = (IFn)const__2.getRawRoot();
            Object object4 = query2;
            query2 = null;
            Object object5 = or__5238__auto__16850 = object4;
            if (object5 != null && object5 != Boolean.FALSE) {
                object3 = or__5238__auto__16850;
                or__5238__auto__16850 = null;
            } else {
                object3 = "";
            }
            object = iFn.invoke((Object)PersistentArrayMap.EMPTY, iFn2.invoke((Object)uri$param_map$fn__16842, iFn3.invoke((Object)uri$param_map$fn__16847, iFn4.invoke(object3, const__3))));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$param_map.invokeStatic(object2);
    }
}

