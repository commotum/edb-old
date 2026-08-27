/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.IDb;
import datomic.log$tx_range$reify__16479;

public final class log$tx_range
extends AFunction {
    public static final Object const__1 = 1000L;
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"t-at-or-since");
    public static final AFn const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 799, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object log2, Object db2, Object start, Object end) {
        Object end2;
        Object object;
        Object object2;
        Object object3 = start;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = start;
            start = null;
            object2 = Numbers.max((long)1000L, (Object)((IFn)const__2.getRawRoot()).invoke(db2, object4));
        } else {
            object2 = const__1;
        }
        Object start2 = object2;
        Object next_t2 = ((IDb)db2).getNextT();
        Object object5 = end;
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = next_t2;
            next_t2 = null;
            Object object7 = db2;
            db2 = null;
            Object object8 = end;
            end = null;
            object = Numbers.min((Object)object6, (Object)((IFn)const__2.getRawRoot()).invoke(object7, object8));
        } else {
            object = next_t2;
            next_t2 = null;
        }
        Object object9 = end2 = object;
        end2 = null;
        Object object10 = log2;
        log2 = null;
        Object object11 = start2;
        start2 = null;
        return ((IObj)new log$tx_range$reify__16479(null, object9, object10, object11)).withMeta((IPersistentMap)const__8);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return log$tx_range.invokeStatic(object5, object6, object7, object8);
    }
}

