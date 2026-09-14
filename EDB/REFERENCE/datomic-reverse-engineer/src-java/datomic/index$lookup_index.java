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
import datomic.index.Index;

public final class index$lookup_index
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"eavt-cmpi");
    public static final Keyword const__4 = RT.keyword(null, (String)"eavt");
    public static final Var const__5 = RT.var((String)"datomic.index", (String)"avet-cmpi");
    public static final Keyword const__6 = RT.keyword(null, (String)"avet");
    public static final Var const__7 = RT.var((String)"datomic.index", (String)"aevt-cmpi");
    public static final Keyword const__8 = RT.keyword(null, (String)"aevt");
    public static final Var const__9 = RT.var((String)"datomic.index", (String)"raet-cmpi");
    public static final Keyword const__10 = RT.keyword(null, (String)"vaet");

    public static Object invokeStatic(Object lookup, Object cmpi, Object rootid) {
        Object object;
        Object object2 = lookup;
        Object object3 = lookup;
        lookup = null;
        Object object4 = rootid;
        rootid = null;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(object3, object4);
        Object object6 = ((IFn)const__1.getRawRoot()).invoke(null);
        if (Util.equiv((Object)cmpi, (Object)const__3.getRawRoot())) {
            object = const__4;
        } else if (Util.equiv((Object)cmpi, (Object)const__5.getRawRoot())) {
            object = const__6;
        } else if (Util.equiv((Object)cmpi, (Object)const__7.getRawRoot())) {
            object = const__8;
        } else {
            Object object7 = cmpi;
            cmpi = null;
            object = Util.equiv((Object)object7, (Object)const__9.getRawRoot()) ? const__10 : null;
        }
        return new Index(object2, cmpi, object5, object6, object);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$lookup_index.invokeStatic(object4, object5, object6);
    }
}

