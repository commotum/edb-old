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

public final class index$drop_avet$fn__15598$fn__15603
extends AFunction {
    Object aid;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"a");

    public index$drop_avet$fn__15598$fn__15603(Object object) {
        this.aid = object;
    }

    public Object invoke(Object p__15602) {
        Object a;
        Object map__15604;
        Object object;
        Object object2 = p__15602;
        p__15602 = null;
        Object map__156042 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__156042);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__156042;
            map__156042 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__156042;
            map__156042 = null;
        }
        Object object5 = map__15604 = object;
        map__15604 = null;
        Object object6 = a = RT.get((Object)object5, (Object)const__3);
        a = null;
        index$drop_avet$fn__15598$fn__15603 this_ = null;
        return Util.equiv((Object)object6, (Object)this_.aid) ? Boolean.TRUE : Boolean.FALSE;
    }
}

