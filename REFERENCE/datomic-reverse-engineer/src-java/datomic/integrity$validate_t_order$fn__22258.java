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

public final class integrity$validate_t_order$fn__22258
extends AFunction {
    Object uri;
    Object progress;
    Object log_fn;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
    public static final Var const__2 = RT.var((String)"datomic.tools", (String)"connection-resources");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__7 = RT.keyword(null, (String)"olookup");
    public static final Keyword const__8 = RT.keyword(null, (String)"create-log-val");
    public static final Var const__9 = RT.var((String)"datomic.log", (String)"create-log-val");
    public static final Var const__10 = RT.var((String)"datomic.api", (String)"db");
    public static final Var const__11 = RT.var((String)"datomic.api", (String)"connect");
    public static final Keyword const__12 = RT.keyword(null, (String)"find-log");
    public static final Var const__13 = RT.var((String)"datomic.log", (String)"find-log");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__15 = RT.var((String)"datomic.integrity", (String)"validate-t-order*");
    public static final Object const__16 = 0L;
    public static final Keyword const__17 = RT.keyword(null, (String)"threw");

    public integrity$validate_t_order$fn__22258(Object object, Object object2, Object object3) {
        this.uri = object;
        this.progress = object2;
        this.log_fn = object3;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Object invoke() {
        try {
            Object object;
            Object temp__5455__auto__22262;
            Object object2;
            Object[] objectArray;
            block10: {
                Object object3;
                objectArray = new Object[2];
                objectArray[0] = const__0;
                this.uri = null;
                Object uri2 = ((IFn)const__1.getRawRoot()).invoke(this.uri);
                Object map__22259 = ((IFn)const__2.getRawRoot()).invoke(uri2);
                Object object4 = ((IFn)const__3.getRawRoot()).invoke(map__22259);
                if (object4 != null && object4 != Boolean.FALSE) {
                    Object object5 = map__22259;
                    map__22259 = null;
                    object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object5)));
                } else {
                    object3 = map__22259;
                    map__22259 = null;
                }
                Object map__222592 = object3;
                Object cluster2 = RT.get((Object)map__222592, (Object)const__6);
                Object object6 = map__222592;
                map__222592 = null;
                Object olookup = RT.get((Object)object6, (Object)const__7);
                Object G__22260 = this.log_fn = null;
                switch (Util.hash((Object)G__22260) >> 3 & 1) {
                    case 0: {
                        if (G__22260 != const__8) break;
                        Object object7 = cluster2;
                        cluster2 = null;
                        Object object8 = olookup;
                        olookup = null;
                        Object object9 = uri2;
                        uri2 = null;
                        object2 = ((IFn)const__9.getRawRoot()).invoke(object7, object8, ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object9)));
                        break block10;
                    }
                    case 1: {
                        if (G__22260 != const__12) break;
                        Object object10 = cluster2;
                        cluster2 = null;
                        Object object11 = olookup;
                        olookup = null;
                        object2 = ((IFn)const__13.getRawRoot()).invoke(object10, object11);
                        break block10;
                    }
                }
                Object object12 = G__22260;
                G__22260 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__14.getRawRoot()).invoke((Object)"No matching clause: ", object12));
            }
            Object object13 = temp__5455__auto__22262 = object2;
            if (object13 != null && object13 != Boolean.FALSE) {
                Object log2;
                Object object14 = temp__5455__auto__22262;
                temp__5455__auto__22262 = null;
                Object object15 = log2 = object14;
                log2 = null;
                this.progress = null;
                object = ((IFn)const__15.getRawRoot()).invoke(object15, this.progress);
            } else {
                object = const__16;
            }
            objectArray[1] = object;
            return RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__17;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            return RT.mapUniqueKeys((Object[])objectArray);
        }
    }
}

