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
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class garbage$gc$fn__19862
extends AFunction {
    Object tstamp;
    Object cluster;
    Object progress;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"uuid");
    public static final Var const__4 = RT.var((String)"datomic.garbage", (String)"gc-dir");
    public static final Keyword const__5 = RT.keyword(null, (String)"count");
    public static final Keyword const__6 = RT.keyword(null, (String)"complete");
    public static final Var const__7 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__8 = RT.keyword(null, (String)"event");
    public static final Keyword const__9 = RT.keyword((String)"garbage", (String)"collect-dir");
    public static final Keyword const__10 = RT.keyword(null, (String)"id");
    public static final Var const__12 = RT.var((String)"datomic.garbage", (String)"gc-delete-vals");
    public static final Var const__13 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");

    public garbage$gc$fn__19862(Object object, Object object2, Object object3) {
        this.tstamp = object;
        this.cluster = object2;
        this.progress = object3;
    }

    public Object invoke(Object total2, Object p__19861) {
        Number number;
        garbage$gc$fn__19862 this_;
        Object object;
        Object map__19863;
        Object object2;
        Object object3 = p__19861;
        p__19861 = null;
        Object map__198632 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__198632);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__198632;
            map__198632 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__198632;
            map__198632 = null;
        }
        Object object6 = map__19863 = object2;
        map__19863 = null;
        Object uuid = RT.get((Object)object6, (Object)const__3);
        Object map__19864 = ((IFn)const__4.getRawRoot()).invoke(this_.cluster, uuid, this_.tstamp);
        Object object7 = ((IFn)const__0.getRawRoot()).invoke(map__19864);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = map__19864;
            map__19864 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object8)));
        } else {
            object = map__19864;
            map__19864 = null;
        }
        Object map__198642 = object;
        Object count2 = RT.get((Object)map__198642, (Object)const__5);
        Object object9 = map__198642;
        map__198642 = null;
        Object complete = RT.get((Object)object9, (Object)const__6);
        ((IFn)this_.progress).invoke(count2);
        Object object10 = complete;
        complete = null;
        if (object10 != null && object10 != Boolean.FALSE) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.garbage");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__7.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__8, const__9, const__10, uuid})));
            }
            Object object11 = total2;
            total2 = null;
            Object object12 = count2;
            count2 = null;
            Object object13 = uuid;
            uuid = null;
            this_ = null;
            number = Numbers.add((Object)Numbers.add((Object)object11, (Object)object12), (Object)((IFn)const__12.getRawRoot()).invoke(this_.cluster, (Object)Tuple.create((Object)((IFn)const__13.getRawRoot()).invoke(object13))));
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

