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

public final class db$filter_assess_tx_datoms$fn__13361
extends AFunction {
    Object es;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"contains?");

    public db$filter_assess_tx_datoms$fn__13361(Object object) {
        this.es = object;
    }

    public Object invoke(Object p__13360) {
        Object e;
        Object map__13362;
        Object object;
        Object object2 = p__13360;
        p__13360 = null;
        Object map__133622 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__133622);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__133622;
            map__133622 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__133622;
            map__133622 = null;
        }
        Object object5 = map__13362 = object;
        map__13362 = null;
        Object object6 = e = RT.get((Object)object5, (Object)const__3);
        e = null;
        db$filter_assess_tx_datoms$fn__13361 this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(this_.es, object6);
    }
}

