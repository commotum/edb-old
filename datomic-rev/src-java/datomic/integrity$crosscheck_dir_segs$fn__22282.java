/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.integrity$crosscheck_dir_segs$fn__22282$fn__22287;

public final class integrity$crosscheck_dir_segs$fn__22282
extends AFunction {
    Object p__22277;
    Object olookup;
    Object map__22278;
    long start__8981__auto__;
    Object m_22279;
    Object cluster;
    Object ___8980__auto__;
    Object progress;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.log", (String)"find-log");
    public static final Var const__2 = RT.var((String)"datomic.log", (String)"seek-tx");
    public static final Object const__3 = 0L;
    public static final Keyword const__4 = RT.keyword(null, (String)"threw");

    public integrity$crosscheck_dir_segs$fn__22282(Object object, Object object2, Object object3, long l, Object object4, Object object5, Object object6, Object object7) {
        this.p__22277 = object;
        this.olookup = object2;
        this.map__22278 = object3;
        this.start__8981__auto__ = l;
        this.m_22279 = object4;
        this.cluster = object5;
        this.___8980__auto__ = object6;
        this.progress = object7;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object temp__5457__auto__22301;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object object2 = temp__5457__auto__22301 = ((IFn)const__1.getRawRoot()).invoke(this.cluster, this.olookup);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object temp__5457__auto__22300;
                Object object3 = temp__5457__auto__22301;
                temp__5457__auto__22301 = null;
                Object log2 = object3;
                Object object4 = temp__5457__auto__22300 = ((IFn)const__2.getRawRoot()).invoke(log2, const__3);
                if (object4 != null && object4 != Boolean.FALSE) {
                    Object tree_iter = temp__5457__auto__22300;
                    this.p__22277 = null;
                    this.olookup = null;
                    Object object5 = log2;
                    log2 = null;
                    this.map__22278 = null;
                    this.m_22279 = null;
                    Object object6 = tree_iter;
                    tree_iter = null;
                    this.cluster = null;
                    this.___8980__auto__ = null;
                    this.progress = null;
                    Object object7 = temp__5457__auto__22300;
                    temp__5457__auto__22300 = null;
                    object = ((IFn)new integrity$crosscheck_dir_segs$fn__22282$fn__22287(this.p__22277, this.olookup, object5, this.map__22278, this.start__8981__auto__, this.m_22279, object6, this.cluster, this.___8980__auto__, this.progress, object7)).invoke();
                } else {
                    object = null;
                }
            } else {
                object = null;
            }
            objectArray[1] = object;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

