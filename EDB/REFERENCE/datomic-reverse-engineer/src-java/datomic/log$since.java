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
 *  clojure.lang.PersistentVector
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
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log$since$fn__16237;

public final class log$since
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"txes");
    public static final Keyword const__4 = RT.keyword(null, (String)"bufs");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"tail-empty?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__9 = RT.var((String)"datomic.log", (String)"tail-ts");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"drop");
    public static final Var const__14 = RT.var((String)"datomic.log", (String)"create-tail");

    public static Object invokeStatic(Object p__16235, Object since_t2) {
        Object object;
        Object object2;
        Object or__5238__auto__16244;
        Object map__16236;
        Object object3;
        Object object4 = p__16235;
        p__16235 = null;
        Object map__162362 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__162362);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__162362;
            map__162362 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__162362;
            map__162362 = null;
        }
        Object tail = map__16236 = object3;
        Object txes = RT.get((Object)map__16236, (Object)const__3);
        Object object7 = map__16236;
        map__16236 = null;
        Object bufs = RT.get((Object)object7, (Object)const__4);
        Object object8 = or__5238__auto__16244 = ((IFn)const__5.getRawRoot()).invoke(since_t2);
        if (object8 != null && object8 != Boolean.FALSE) {
            object2 = or__5238__auto__16244;
            or__5238__auto__16244 = null;
        } else {
            Object or__5238__auto__16243;
            Object object9 = or__5238__auto__16243 = ((IFn)const__6.getRawRoot()).invoke(tail);
            if (object9 != null && object9 != Boolean.FALSE) {
                object2 = or__5238__auto__16243;
                or__5238__auto__16243 = null;
            } else {
                object2 = Numbers.lt((Object)since_t2, (Object)((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(tail))) ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = tail;
            tail = null;
        } else {
            Object object10 = since_t2;
            since_t2 = null;
            Object object11 = txes;
            txes = null;
            Object new_txes = ((IFn)new log$since$fn__16237(object10, object11)).invoke();
            int bufs_ct = RT.count((Object)bufs);
            Object object12 = bufs;
            bufs = null;
            Object new_bufs = ((IFn)const__11.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__12.getRawRoot()).invoke((Object)Numbers.num((long)Numbers.minus((long)bufs_ct, (long)RT.count((Object)new_txes)))), object12);
            Object object13 = new_txes;
            new_txes = null;
            Object object14 = new_bufs;
            new_bufs = null;
            object = ((IFn)const__14.getRawRoot()).invoke(object13, object14);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$since.invokeStatic(object3, object4);
    }
}

