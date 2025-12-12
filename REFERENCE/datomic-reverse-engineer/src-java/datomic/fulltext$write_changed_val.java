/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class fulltext$write_changed_val
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cstore, Object oldid, Object oldval, Object newval, Object garbage) {
        block5: {
            block4: {
                v0 = oldval;
                oldval = null;
                if (!Util.equiv((Object)v0, (Object)newval)) break block4;
                v1 = oldid;
                oldid = null;
                v2 = garbage;
                garbage = null;
                v3 = Tuple.create((Object)v1, (Object)v2);
                break block5;
            }
            id = ((IFn)fulltext$write_changed_val.const__1.getRawRoot()).invoke();
            v4 = cstore;
            cstore = null;
            v5 = v4;
            if (Util.classOf((Object)v4) == fulltext$write_changed_val.__cached_class__0) ** GOTO lbl19
            if (!(v5 instanceof ClusteredStore)) {
                v5 = v5;
                fulltext$write_changed_val.__cached_class__0 = Util.classOf((Object)v5);
lbl19:
                // 2 sources

                v6 = newval;
                newval = null;
                v7 = fulltext$write_changed_val.const__2.getRawRoot().invoke(v5, ((IFn)fulltext$write_changed_val.const__3.getRawRoot()).invoke(id), ((IFn)fulltext$write_changed_val.const__4.getRawRoot()).invoke(v6, fulltext$write_changed_val.const__5.getRawRoot()));
            } else {
                v8 = newval;
                newval = null;
                v7 = result = ((ClusteredStore)v5).create_val(((IFn)fulltext$write_changed_val.const__3.getRawRoot()).invoke(id), ((IFn)fulltext$write_changed_val.const__4.getRawRoot()).invoke(v8, fulltext$write_changed_val.const__5.getRawRoot()));
            }
            if (Util.equiv((Object)fulltext$write_changed_val.const__6, (Object)((IFn)fulltext$write_changed_val.const__7.getRawRoot()).invoke(result))) {
                v9 = id;
                id = null;
                v10 = garbage;
                garbage = null;
                v11 = oldid;
                oldid = null;
                v3 = Tuple.create((Object)v9, (Object)((IFn)fulltext$write_changed_val.const__8.getRawRoot()).invoke(v10, ((IFn)fulltext$write_changed_val.const__3.getRawRoot()).invoke(v11)));
            } else {
                v12 = result;
                result = null;
                throw (Throwable)new Error((String)((IFn)fulltext$write_changed_val.const__9.getRawRoot()).invoke((Object)"value write failed ", ((IFn)fulltext$write_changed_val.const__7.getRawRoot()).invoke(v12)));
            }
        }
        return v3;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return fulltext$write_changed_val.invokeStatic(object6, object7, object8, object9, object10);
    }

    static {
        const__1 = RT.var((String)"datomic.common", (String)"rand-uuid");
        const__2 = RT.var((String)"datomic.cluster", (String)"create-val");
        const__3 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__4 = RT.var((String)"datomic.fressian", (String)"fressian-val");
        const__5 = RT.var((String)"datomic.fulltext", (String)"write-handlers");
        const__6 = RT.keyword(null, (String)"created");
        const__7 = RT.var((String)"clojure.core", (String)"deref");
        const__8 = RT.var((String)"clojure.core", (String)"conj");
        const__9 = RT.var((String)"clojure.core", (String)"str");
    }
}

