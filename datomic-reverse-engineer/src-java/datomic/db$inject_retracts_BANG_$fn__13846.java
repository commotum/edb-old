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
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db.IProcess;
import java.util.Map;

public final class db$inject_retracts_BANG_$fn__13846
extends AFunction {
    Object local_tempids;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Object const__6 = 2L;

    public db$inject_retracts_BANG_$fn__13846(Object object) {
        this.local_tempids = object;
    }

    public Object invoke(Object proc, Object p__13845) {
        Object object;
        Object object2 = p__13845;
        p__13845 = null;
        Object map__13847 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__13847);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__13847;
            map__13847 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__13847;
            map__13847 = null;
        }
        Object map__138472 = object;
        Object e = RT.get((Object)map__138472, (Object)const__3);
        Object a = RT.get((Object)map__138472, (Object)const__4);
        Object object5 = map__138472;
        map__138472 = null;
        Object v = RT.get((Object)object5, (Object)const__5);
        Object object6 = proc;
        proc = null;
        Object object7 = e;
        e = null;
        Object object8 = a;
        a = null;
        Object object9 = v;
        v = null;
        return ((IProcess)object6).inject(Tuple.create((Object)const__6, (Object)object7, (Object)object8, (Object)object9), (Map)this.local_tempids);
    }
}

