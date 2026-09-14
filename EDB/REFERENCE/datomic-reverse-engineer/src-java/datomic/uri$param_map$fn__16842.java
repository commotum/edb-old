/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.regex.Pattern;

public final class uri$param_map$fn__16842
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__4 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__5 = Pattern.compile("_");

    public Object invoke(Object p__16841) {
        Object object = p__16841;
        p__16841 = null;
        Object vec__16843 = object;
        Object k = RT.nth((Object)vec__16843, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16843;
        vec__16843 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        Object object4 = v;
        v = null;
        return Tuple.create((Object)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object3, const__5, (Object)"-")), (Object)object4);
    }
}

