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

public final class tools$uniques_in_ts$fn__21860
extends AFunction {
    Object ids;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"a");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"contains?");

    public tools$uniques_in_ts$fn__21860(Object object) {
        this.ids = object;
    }

    public Object invoke(Object p__21859) {
        Object a;
        Object map__21861;
        Object object;
        Object object2 = p__21859;
        p__21859 = null;
        Object map__218612 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__218612);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__218612;
            map__218612 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__218612;
            map__218612 = null;
        }
        Object object5 = map__21861 = object;
        map__21861 = null;
        Object object6 = a = RT.get((Object)object5, (Object)const__3);
        a = null;
        tools$uniques_in_ts$fn__21860 this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(this_.ids, object6);
    }
}

