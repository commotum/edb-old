/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$push_preds$fn__18585$fn__18595;
import datomic.datalog$push_preds$fn__18585$fn__18597;

public final class datalog$push_preds$fn__18585
extends AFunction {
    Object pred_QMARK_;
    Object ret;
    Object ctor;
    Object G__18578;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"split-with");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__10 = RT.var((String)"datomic.datalog", (String)"truep");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"map");

    public datalog$push_preds$fn__18585(Object object, Object object2, Object object3, Object object4) {
        this.pred_QMARK_ = object;
        this.ret = object2;
        this.ctor = object3;
        this.G__18578 = object4;
    }

    public Object invoke() {
        Object ret;
        Object ret2 = this.ret = null;
        Object G__18578 = this.G__18578 = null;
        while (true) {
            Object object;
            Object sched;
            Object object2 = ret2;
            ret2 = null;
            ret = object2;
            Object object3 = G__18578;
            G__18578 = null;
            Object vec__18586 = object3;
            Object seq__18587 = ((IFn)const__0.getRawRoot()).invoke(vec__18586);
            Object first__18588 = ((IFn)const__1.getRawRoot()).invoke(seq__18587);
            Object object4 = seq__18587;
            seq__18587 = null;
            Object seq__185872 = ((IFn)const__2.getRawRoot()).invoke(object4);
            Object object5 = first__18588;
            first__18588 = null;
            Object vec__18589 = object5;
            Object clause = RT.nth((Object)vec__18589, (int)RT.uncheckedIntCast((long)0L), null);
            Object object6 = vec__18589;
            vec__18589 = null;
            Object outbinds = RT.nth((Object)object6, (int)RT.uncheckedIntCast((long)1L), null);
            Object object7 = seq__185872;
            seq__185872 = null;
            Object clauses = object7;
            Object object8 = vec__18586;
            vec__18586 = null;
            Object object9 = sched = object8;
            sched = null;
            if (object9 == null || object9 == Boolean.FALSE) break;
            Object object10 = ((IFn)this.pred_QMARK_).invoke(clause);
            if (object10 != null && object10 != Boolean.FALSE) {
                Object object11 = ret;
                ret = null;
                Object object12 = clause;
                clause = null;
                Object object13 = outbinds;
                outbinds = null;
                Object object14 = clauses;
                clauses = null;
                G__18578 = ((IFn)const__0.getRawRoot()).invoke(object14);
                ret2 = ((IFn)const__6.getRawRoot()).invoke(object11, (Object)Tuple.create((Object)object12, (Object)object13, null));
                continue;
            }
            Object object15 = clauses;
            clauses = null;
            Object vec__18592 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(this.pred_QMARK_, const__1.getRawRoot()), object15);
            Object preds = RT.nth((Object)vec__18592, (int)RT.uncheckedIntCast((long)0L), null);
            Object object16 = vec__18592;
            vec__18592 = null;
            Object clauses2 = RT.nth((Object)object16, (int)RT.uncheckedIntCast((long)1L), null);
            IFn iFn = (IFn)const__6.getRawRoot();
            Object object17 = ret;
            ret = null;
            Object object18 = clause;
            clause = null;
            Object object19 = ((IFn)const__9.getRawRoot()).invoke(preds);
            if (object19 != null && object19 != Boolean.FALSE) {
                object = const__10.getRawRoot();
            } else {
                Object object20 = outbinds;
                outbinds = null;
                Object object21 = preds;
                preds = null;
                Object ctors = ((IFn)const__11.getRawRoot()).invoke((Object)new datalog$push_preds$fn__18585$fn__18595(this.ctor, object20), object21);
                ctors = null;
                object = new datalog$push_preds$fn__18585$fn__18597(ctors);
            }
            Object object22 = clauses2;
            clauses2 = null;
            G__18578 = ((IFn)const__0.getRawRoot()).invoke(object22);
            ret2 = iFn.invoke(object17, (Object)Tuple.create((Object)object18, (Object)outbinds, (Object)object));
        }
        Object var3_3 = null;
        return ret;
    }
}

