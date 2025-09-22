/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class extension_resolver$user_namespaces
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.extension-resolver", (String)"load-extensions-config");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"xforms");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"nil?");

    public static Object invokeStatic(Object path2) {
        Object xforms;
        Object map__14325;
        Object object;
        Object object2 = path2;
        path2 = null;
        Object map__143252 = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(map__143252);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__143252;
            map__143252 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object4)));
        } else {
            object = map__143252;
            map__143252 = null;
        }
        Object object5 = map__14325 = object;
        map__14325 = null;
        Object object6 = xforms = RT.get((Object)object5, (Object)const__4);
        xforms = null;
        return ((IFn)const__5.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), object6)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$user_namespaces.invokeStatic(object2);
    }
}

