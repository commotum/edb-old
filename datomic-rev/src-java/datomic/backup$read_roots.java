/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.Storage;

public final class backup$read_roots
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object t, Object from_storage) {
        v0 = backup$read_roots.__thunk__0__;
        v1 = from_storage;
        from_storage = null;
        v2 = v1;
        if (Util.classOf((Object)v1) == backup$read_roots.__cached_class__0) ** GOTO lbl9
        if (!(v2 instanceof Storage)) {
            v2 = v2;
            backup$read_roots.__cached_class__0 = Util.classOf((Object)v2);
lbl9:
            // 2 sources

            v3 = t;
            t = null;
            v4 = backup$read_roots.const__1.getRawRoot().invoke(v2, ((IFn)backup$read_roots.const__2.getRawRoot()).invoke(v3));
        } else {
            v5 = t;
            t = null;
            v4 = ((Storage)v2).retrieve(((IFn)backup$read_roots.const__2.getRawRoot()).invoke(v5));
        }
        if (v0 == (v6 = v0.get(v4))) {
            backup$read_roots.__thunk__0__ = backup$read_roots.__site__0__.fault(v4);
            v6 = backup$read_roots.__thunk__0__.get(v4);
        }
        v7 = temp__5457__auto__20219 = v6;
        if (v7 != null && v7 != Boolean.FALSE) {
            v8 = temp__5457__auto__20219;
            temp__5457__auto__20219 = null;
            v9 = buf = v8;
            buf = null;
            v10 = ((IFn)backup$read_roots.const__3.getRawRoot()).invoke(((IFn)backup$read_roots.const__4.getRawRoot()).invoke(v9, (Object)backup$read_roots.const__5, backup$read_roots.const__6.getRawRoot()));
        } else {
            v10 = null;
        }
        return v10;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$read_roots.invokeStatic(object3, object4);
    }

    static {
        const__1 = RT.var((String)"datomic.backup", (String)"retrieve");
        const__2 = RT.var((String)"datomic.backup", (String)"roots-path");
        const__3 = RT.var((String)"datomic.backup", (String)"backup->mem");
        const__4 = RT.var((String)"datomic.fressian", (String)"defressian");
        const__5 = RT.keyword(null, (String)"handlers");
        const__6 = RT.var((String)"datomic.fressian", (String)"user-read-handlers");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
        __thunk__0__ = __site__0__;
    }
}

