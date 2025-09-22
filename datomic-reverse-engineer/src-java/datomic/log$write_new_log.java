/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.log$write_new_log$fn__16345;

public final class log$write_new_log
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"repeatedly");
    public static final Object const__2 = 0L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__5 = RT.var((String)"datomic.log", (String)"zip-and-create");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"fressianed-dir");
    public static final Var const__7 = RT.var((String)"datomic.log", (String)"create-entry");
    public static final Var const__8 = RT.var((String)"datomic.log", (String)"empty-tail");
    public static final Var const__9 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
    public static final Keyword const__10 = RT.keyword(null, (String)"rev");
    public static final Keyword const__11 = RT.keyword(null, (String)"etag");
    public static final Keyword const__12 = RT.keyword((String)"d", (String)"r");
    public static final Keyword const__13 = RT.keyword((String)"d", (String)"l");
    public static final Object const__14 = 3L;
    public static final Var const__15 = RT.var((String)"datomic.log", (String)"write-tail-descriptor");
    public static final Var const__16 = RT.var((String)"datomic.log", (String)"BEGIN_OPEN_LIST");

    public static Object invokeStatic(Object cs) {
        IPersistentVector iPersistentVector;
        Object temp__5457__auto__16348;
        Object vec__16342 = ((IFn)const__0.getRawRoot()).invoke((Object)new log$write_new_log$fn__16345());
        Object new_root_id = RT.nth((Object)vec__16342, (int)RT.intCast((long)0L), null);
        Object object = vec__16342;
        vec__16342 = null;
        Object new_tail_id = RT.nth((Object)object, (int)RT.intCast((long)1L), null);
        ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(cs, new_root_id, ((IFn)const__6.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)const__7.getRawRoot()).invoke(const__2, new_tail_id)))));
        Object object2 = new_tail_id;
        new_tail_id = null;
        ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(cs, object2, ((IFn)const__6.getRawRoot()).invoke((Object)PersistentVector.EMPTY)));
        Object tail = ((IFn)const__8.getRawRoot()).invoke();
        Object object3 = new_root_id;
        new_root_id = null;
        Object root_id2 = ((IFn)const__9.getRawRoot()).invoke(object3);
        Object[] objectArray = new Object[8];
        objectArray[0] = const__10;
        objectArray[1] = const__2;
        objectArray[2] = const__11;
        objectArray[3] = null;
        objectArray[4] = const__12;
        Object object4 = root_id2;
        root_id2 = null;
        objectArray[5] = object4;
        objectArray[6] = const__13;
        objectArray[7] = const__14;
        IPersistentMap proposed_desc = RT.mapUniqueKeys((Object[])objectArray);
        Object object5 = cs;
        cs = null;
        IPersistentMap iPersistentMap = proposed_desc;
        proposed_desc = null;
        Object object6 = temp__5457__auto__16348 = ((IFn)const__15.getRawRoot()).invoke(object5, (Object)iPersistentMap, const__16.getRawRoot());
        if (object6 != null && object6 != Boolean.FALSE) {
            Object desc;
            Object object7 = temp__5457__auto__16348;
            temp__5457__auto__16348 = null;
            Object object8 = desc = object7;
            desc = null;
            Object object9 = tail;
            tail = null;
            iPersistentVector = Tuple.create((Object)object8, (Object)object9);
        } else {
            iPersistentVector = null;
        }
        return iPersistentVector;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$write_new_log.invokeStatic(object2);
    }
}

