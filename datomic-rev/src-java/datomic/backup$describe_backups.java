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
import datomic.backup$describe_backups$fn__20222;
import datomic.backup$describe_backups$fn__20224;
import datomic.backup.Storage;

public final class backup$describe_backups
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Var const__3;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__10;
    public static final Var const__11;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object storage) {
        v0 = ((IFn)backup$describe_backups.const__0.getRawRoot()).invoke(storage);
        if (v0 != null && v0 != Boolean.FALSE) {
            v1 = storage;
            storage = null;
            v2 = ((IFn)backup$describe_backups.const__1.getRawRoot()).invoke(v1);
        } else {
            v2 = storage;
            storage = null;
        }
        storage = v2;
        v3 = new Object[4];
        v3[0] = backup$describe_backups.const__2;
        v3[1] = ((IFn)backup$describe_backups.const__3.getRawRoot()).invoke(storage);
        v3[2] = backup$describe_backups.const__4;
        v4 = (IFn)backup$describe_backups.const__5.getRawRoot();
        v5 = backup$describe_backups.const__6.getRawRoot();
        v6 = (IFn)backup$describe_backups.const__7.getRawRoot();
        v7 = new backup$describe_backups$fn__20222();
        v8 = (IFn)backup$describe_backups.const__8.getRawRoot();
        v9 = new backup$describe_backups$fn__20224();
        v10 = backup$describe_backups.__thunk__0__;
        v11 = storage;
        storage = null;
        v12 = ((IFn)backup$describe_backups.const__11.getRawRoot()).invoke(v11, (Object)"roots");
        if (Util.classOf((Object)v12) == backup$describe_backups.__cached_class__0) ** GOTO lbl28
        if (!(v12 instanceof Storage)) {
            v12 = v12;
            backup$describe_backups.__cached_class__0 = Util.classOf((Object)v12);
lbl28:
            // 2 sources

            v13 = backup$describe_backups.const__10.getRawRoot().invoke(v12, (Object)"");
        } else {
            v13 = ((Storage)v12).list_keys("");
        }
        if (v10 == (v14 = v10.get(v13))) {
            backup$describe_backups.__thunk__0__ = backup$describe_backups.__site__0__.fault(v13);
            v14 = backup$describe_backups.__thunk__0__.get(v13);
        }
        v3[3] = v4.invoke(v5, v6.invoke((Object)v7, v8.invoke((Object)v9, v14)));
        return RT.mapUniqueKeys((Object[])v3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$describe_backups.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"string?");
        const__1 = RT.var((String)"datomic.backup", (String)"create-storage");
        const__2 = RT.keyword(null, (String)"db-id");
        const__3 = RT.var((String)"datomic.backup", (String)"claimed-by");
        const__4 = RT.keyword(null, (String)"ts");
        const__5 = RT.var((String)"clojure.core", (String)"sort");
        const__6 = RT.var((String)"clojure.core", (String)">");
        const__7 = RT.var((String)"clojure.core", (String)"mapv");
        const__8 = RT.var((String)"clojure.core", (String)"filter");
        const__10 = RT.var((String)"datomic.backup", (String)"list-keys");
        const__11 = RT.var((String)"datomic.backup", (String)"substorage");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"ks"));
        __thunk__0__ = __site__0__;
    }
}

