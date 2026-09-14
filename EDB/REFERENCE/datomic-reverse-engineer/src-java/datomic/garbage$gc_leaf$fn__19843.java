/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class garbage$gc_leaf$fn__19843
extends AFunction {
    Object cluster;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"vals");
    public static final Var const__5 = RT.var((String)"datomic.garbage", (String)"gc-delete-vals");

    public garbage$gc_leaf$fn__19843(Object object) {
        this.cluster = object;
    }

    public Object invoke(Object count2, Object p__19842) {
        Object map__19844;
        Object object;
        Object object2 = p__19842;
        p__19842 = null;
        Object map__198442 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__198442);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__198442;
            map__198442 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__198442;
            map__198442 = null;
        }
        Object object5 = map__19844 = object;
        map__19844 = null;
        Object vals = RT.get((Object)object5, (Object)const__3);
        Object object6 = count2;
        count2 = null;
        Object object7 = vals;
        vals = null;
        garbage$gc_leaf$fn__19843 this_ = null;
        return Numbers.add((Object)object6, (Object)((IFn)const__5.getRawRoot()).invoke(this_.cluster, object7));
    }
}

