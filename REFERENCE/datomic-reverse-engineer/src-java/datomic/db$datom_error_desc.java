/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.db.Attribute;

public final class db$datom_error_desc
extends AFunction {
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"v-error-desc");

    public static Object invokeStatic(Object db2, Object p__13028) {
        IPersistentVector iPersistentVector;
        Object object;
        Object object2;
        Object and__5236__auto__13034;
        Object object3;
        Object object4 = p__13028;
        p__13028 = null;
        Object vec__13029 = object4;
        Object e = RT.nth((Object)vec__13029, (int)RT.uncheckedIntCast((long)0L), null);
        Object a = RT.nth((Object)vec__13029, (int)RT.uncheckedIntCast((long)1L), null);
        Object v = RT.nth((Object)vec__13029, (int)RT.uncheckedIntCast((long)2L), null);
        Object tx = RT.nth((Object)vec__13029, (int)RT.uncheckedIntCast((long)3L), null);
        Object object5 = vec__13029;
        vec__13029 = null;
        Object added = RT.nth((Object)object5, (int)RT.uncheckedIntCast((long)4L), null);
        Object G__13032 = ((Database)db2).entid(a);
        if (Util.identical((Object)G__13032, null)) {
            object3 = null;
        } else {
            Object object6 = G__13032;
            G__13032 = null;
            object3 = ((IFn)const__7.getRawRoot()).invoke(db2, object6);
        }
        Object attr = object3;
        IFn iFn = (IFn)const__8.getRawRoot();
        Object object7 = e;
        e = null;
        Object object8 = ((IFn)const__9.getRawRoot()).invoke(db2, object7);
        Object object9 = a;
        a = null;
        Object object10 = ((IFn)const__9.getRawRoot()).invoke(db2, object9);
        Object object11 = and__5236__auto__13034 = attr;
        if (object11 != null && object11 != Boolean.FALSE) {
            Object object12 = attr;
            attr = null;
            object2 = Util.equiv((long)20L, (Object)((Attribute)object12).vtypeid) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object2 = and__5236__auto__13034;
            and__5236__auto__13034 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object13 = db2;
            db2 = null;
            Object object14 = v;
            v = null;
            object = ((IFn)const__9.getRawRoot()).invoke(object13, object14);
        } else {
            Object object15 = v;
            v = null;
            object = ((IFn)const__12.getRawRoot()).invoke(object15);
        }
        IPersistentVector iPersistentVector2 = Tuple.create((Object)object8, (Object)object10, (Object)object);
        Object object16 = tx;
        if (object16 != null && object16 != Boolean.FALSE) {
            Object object17 = tx;
            tx = null;
            Object object18 = added;
            added = null;
            iPersistentVector = Tuple.create((Object)object17, (Object)object18);
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke((Object)iPersistentVector2, iPersistentVector);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$datom_error_desc.invokeStatic(object3, object4);
    }
}

