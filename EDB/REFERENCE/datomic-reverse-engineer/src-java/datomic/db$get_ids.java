/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$get_ids$fn__13872;
import datomic.db$get_ids$fn__13881;
import datomic.db$get_ids$genid__13875;
import datomic.db.Db;

public final class db$get_ids
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object db2, Object data2, Object part_reqs) {
        db$get_ids$genid__13875 genid;
        Delay default_part_ref;
        Delay delay = default_part_ref = new Delay((IFn)new db$get_ids$fn__13872(db2));
        default_part_ref = null;
        Object object = part_reqs;
        part_reqs = null;
        db$get_ids$genid__13875 db$get_ids$genid__13875 = genid = new db$get_ids$genid__13875(db2, delay, object);
        genid = null;
        db$get_ids$fn__13881 db$get_ids$fn__13881 = new db$get_ids$fn__13881(db2, (Object)db$get_ids$genid__13875);
        Object object2 = db2;
        db2 = null;
        Object object3 = data2;
        data2 = null;
        Object vec__13869 = ((IFn)const__0.getRawRoot()).invoke((Object)db$get_ids$fn__13881, (Object)Tuple.create((Object)PersistentArrayMap.EMPTY, (Object)RT.count((Object)((Db)db2).elements), (Object)Numbers.num((long)Numbers.unchecked_inc((long)((Db)object2).nextT())), (Object)PersistentArrayMap.EMPTY, (Object)Boolean.FALSE), object3);
        Object m = RT.nth((Object)vec__13869, (int)RT.uncheckedIntCast((long)0L), null);
        RT.nth((Object)vec__13869, (int)RT.uncheckedIntCast((long)1L), null);
        RT.nth((Object)vec__13869, (int)RT.uncheckedIntCast((long)2L), null);
        RT.nth((Object)vec__13869, (int)RT.uncheckedIntCast((long)3L), null);
        Object object4 = vec__13869;
        vec__13869 = null;
        Object z = RT.nth((Object)object4, (int)RT.uncheckedIntCast((long)4L), null);
        Object object5 = m;
        m = null;
        Object object6 = z;
        z = null;
        return Tuple.create((Object)object5, (Object)object6);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$get_ids.invokeStatic(object4, object5, object6);
    }
}

