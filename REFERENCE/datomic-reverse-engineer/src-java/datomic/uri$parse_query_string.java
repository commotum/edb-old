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
import datomic.uri$parse_query_string$fn__16826;
import datomic.uri$parse_query_string$fn__16828;
import java.util.regex.Pattern;

public final class uri$parse_query_string
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.string", (String)"split");
    public static final Object const__4 = Pattern.compile("&");

    public static Object invokeStatic(Object q2) {
        Object object;
        Object or__5238__auto__16831;
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        uri$parse_query_string$fn__16826 uri$parse_query_string$fn__16826 = new uri$parse_query_string$fn__16826();
        IFn iFn3 = (IFn)const__2.getRawRoot();
        uri$parse_query_string$fn__16828 uri$parse_query_string$fn__16828 = new uri$parse_query_string$fn__16828();
        IFn iFn4 = (IFn)const__3.getRawRoot();
        Object object2 = q2;
        q2 = null;
        Object object3 = or__5238__auto__16831 = object2;
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__16831;
            or__5238__auto__16831 = null;
        } else {
            object = "";
        }
        return iFn.invoke((Object)PersistentArrayMap.EMPTY, iFn2.invoke((Object)uri$parse_query_string$fn__16826, iFn3.invoke((Object)uri$parse_query_string$fn__16828, iFn4.invoke(object, const__4))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$parse_query_string.invokeStatic(object2);
    }
}

