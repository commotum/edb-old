/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.summary.Summary;

public final class domain$fn__16786
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__4;
    public static final Object const__5;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object o, Object w) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = w;
        w = null;
        IFn iFn2 = (IFn)const__1.getRawRoot();
        IFn iFn3 = (IFn)const__2.getRawRoot();
        Object object3 = o;
        o = null;
        Object object4 = object3;
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object4 instanceof Summary) {
                object = ((Summary)object4).summary();
                return iFn.invoke(object2, iFn2.invoke(iFn3.invoke(object, (Object)const__4, const__5)));
            }
            object4 = object4;
            __cached_class__0 = Util.classOf((Object)object4);
        }
        object = const__3.getRawRoot().invoke(object4);
        return iFn.invoke(object2, iFn2.invoke(iFn3.invoke(object, (Object)const__4, const__5)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return domain$fn__16786.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.summary", (String)"write");
        const__1 = RT.var((String)"clojure.core", (String)"str");
        const__2 = RT.var((String)"clojure.core", (String)"assoc");
        const__3 = RT.var((String)"datomic.summary", (String)"summary");
        const__4 = RT.keyword(null, (String)"type");
        const__5 = RT.classForName((String)"datomic.domain.ValcachePoller");
    }
}

