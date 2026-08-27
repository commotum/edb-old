/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.Storage;

public final class backup$claimed_by
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__4;
    public static final Var const__5;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object storage) {
        v0 = storage;
        storage = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == backup$claimed_by.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof Storage)) {
            v1 = v1;
            backup$claimed_by.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = backup$claimed_by.const__0.getRawRoot().invoke(v1, (Object)"owner");
        } else {
            v2 = ((Storage)v1).retrieve("owner");
        }
        v3 = temp__5457__auto__20023 = v2;
        if (v3 != null && v3 != Boolean.FALSE) {
            v4 = temp__5457__auto__20023;
            temp__5457__auto__20023 = null;
            map__20021 = v4;
            v5 = ((IFn)backup$claimed_by.const__1.getRawRoot()).invoke(map__20021);
            if (v5 != null && v5 != Boolean.FALSE) {
                v6 = map__20021;
                map__20021 = null;
                v7 = PersistentHashMap.create((ISeq)((ISeq)((IFn)backup$claimed_by.const__2.getRawRoot()).invoke(v6)));
            } else {
                v7 = map__20021;
                map__20021 = null;
            }
            v8 = map__20021 = v7;
            map__20021 = null;
            v9 = v = RT.get((Object)v8, (Object)backup$claimed_by.const__4);
            v = null;
            v10 = ((IFn)backup$claimed_by.const__5.getRawRoot()).invoke(v9);
        } else {
            v10 = null;
        }
        return v10;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$claimed_by.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"retrieve");
        const__1 = RT.var((String)"clojure.core", (String)"seq?");
        const__2 = RT.var((String)"clojure.core", (String)"seq");
        const__4 = RT.keyword(null, (String)"v");
        const__5 = RT.var((String)"datomic.io", (String)"bbuf->string");
    }
}

