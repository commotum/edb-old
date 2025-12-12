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
 *  clojure.lang.Tuple
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
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class garbage$gc_dir$fn__19852
extends AFunction {
    Object cluster;
    Object tstamp;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"uuid");
    public static final Var const__4 = RT.var((String)"datomic.garbage", (String)"gc-leaf");
    public static final Keyword const__5 = RT.keyword(null, (String)"count");
    public static final Keyword const__6 = RT.keyword(null, (String)"complete");
    public static final Var const__8 = RT.var((String)"datomic.garbage", (String)"gc-delete-vals");
    public static final Var const__9 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");

    public garbage$gc_dir$fn__19852(Object object, Object object2) {
        this.cluster = object;
        this.tstamp = object2;
    }

    public Object invoke(Object total2, Object p__19851) {
        Number number;
        garbage$gc_dir$fn__19852 this_;
        Object complete;
        Object object;
        Object map__19853;
        Object object2;
        Object object3 = p__19851;
        p__19851 = null;
        Object map__198532 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__198532);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__198532;
            map__198532 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__198532;
            map__198532 = null;
        }
        Object object6 = map__19853 = object2;
        map__19853 = null;
        Object uuid = RT.get((Object)object6, (Object)const__3);
        Object map__19854 = ((IFn)const__4.getRawRoot()).invoke(this_.cluster, uuid, this_.tstamp);
        Object object7 = ((IFn)const__0.getRawRoot()).invoke(map__19854);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = map__19854;
            map__19854 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object8)));
        } else {
            object = map__19854;
            map__19854 = null;
        }
        Object map__198542 = object;
        Object count2 = RT.get((Object)map__198542, (Object)const__5);
        Object object9 = map__198542;
        map__198542 = null;
        Object object10 = complete = RT.get((Object)object9, (Object)const__6);
        complete = null;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = total2;
            total2 = null;
            Object object12 = count2;
            count2 = null;
            Object object13 = uuid;
            uuid = null;
            this_ = null;
            number = Numbers.add((Object)Numbers.add((Object)object11, (Object)object12), (Object)((IFn)const__8.getRawRoot()).invoke(this_.cluster, (Object)Tuple.create((Object)((IFn)const__9.getRawRoot()).invoke(object13))));
        } else {
            Object object14 = total2;
            total2 = null;
            Object object15 = count2;
            count2 = null;
            this_ = null;
            number = Numbers.add((Object)object14, (Object)object15);
        }
        return number;
    }
}

