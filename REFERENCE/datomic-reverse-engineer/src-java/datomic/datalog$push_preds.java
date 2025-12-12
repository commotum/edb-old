/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datalog$push_preds$ctor__18558;
import datomic.datalog$push_preds$fn__18585;
import datomic.datalog$push_preds$pred_QMARK___18554;

public final class datalog$push_preds
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object srcs, Object sched) {
        Object G__18578;
        datalog$push_preds$pred_QMARK___18554 pred_QMARK_ = new datalog$push_preds$pred_QMARK___18554();
        Object object = srcs;
        srcs = null;
        datalog$push_preds$ctor__18558 ctor = new datalog$push_preds$ctor__18558(object);
        Object ret = PersistentVector.EMPTY;
        Object object2 = sched;
        sched = null;
        Object vec__18579 = G__18578 = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object seq__18580 = ((IFn)const__0.getRawRoot()).invoke(vec__18579);
        Object first__18581 = ((IFn)const__1.getRawRoot()).invoke(seq__18580);
        Object object3 = seq__18580;
        seq__18580 = null;
        Object seq__185802 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object object4 = first__18581;
        first__18581 = null;
        Object vec__18582 = object4;
        RT.nth((Object)vec__18582, (int)RT.uncheckedIntCast((long)0L), null);
        Object object5 = vec__18582;
        vec__18582 = null;
        RT.nth((Object)object5, (int)RT.uncheckedIntCast((long)1L), null);
        seq__185802 = null;
        vec__18579 = null;
        datalog$push_preds$pred_QMARK___18554 datalog$push_preds$pred_QMARK___18554 = pred_QMARK_;
        pred_QMARK_ = null;
        PersistentVector persistentVector = ret;
        ret = null;
        datalog$push_preds$ctor__18558 datalog$push_preds$ctor__18558 = ctor;
        ctor = null;
        Object object6 = G__18578;
        G__18578 = null;
        Object object7 = ret = ((IFn)new datalog$push_preds$fn__18585((Object)datalog$push_preds$pred_QMARK___18554, persistentVector, (Object)datalog$push_preds$ctor__18558, object6)).invoke();
        ret = null;
        return object7;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$push_preds.invokeStatic(object3, object4);
    }
}

