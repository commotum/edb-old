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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$latest_t
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"describe-backups");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"ts");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object uri2) {
        Object ts;
        Object map__20246;
        Object object;
        Object object2 = uri2;
        uri2 = null;
        Object map__202462 = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(map__202462);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__202462;
            map__202462 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object4)));
        } else {
            object = map__202462;
            map__202462 = null;
        }
        Object object5 = map__20246 = object;
        map__20246 = null;
        Object object6 = ts = RT.get((Object)object5, (Object)const__4);
        ts = null;
        return ((IFn)const__5.getRawRoot()).invoke(object6);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$latest_t.invokeStatic(object2);
    }
}

