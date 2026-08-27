/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datalog$sched_in_order$cargs__18477;
import datomic.datalog$sched_in_order$cbinds__18489;
import datomic.datalog$sched_in_order$delay_ins__18500;
import datomic.datalog$sched_in_order$extdb__18480;
import datomic.datalog$sched_in_order$fn__18539;
import datomic.datalog$sched_in_order$fn__18547;
import datomic.datalog$sched_in_order$in_clause_QMARK___18495;
import datomic.datalog$sched_in_order$pack__18473;
import datomic.datalog$sched_in_order$pred_QMARK___18514;
import datomic.datalog$sched_in_order$reqcnt__18517;
import datomic.datalog$sched_in_order$src__18470;
import datomic.datalog$sched_in_order$sv_clause_QMARK___18491;
import datomic.datalog$sched_in_order$underbound_QMARK___18522;
import datomic.datalog$sched_in_order$unpack__18475;

public final class datalog$sched_in_order
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"split-filter");
    public static final Var const__7 = RT.var((String)"datomic.datalog", (String)"not-join-clause?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"reductions");
    public static final Var const__13 = RT.var((String)"clojure.set", (String)"union");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"nnext");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"reverse");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"pop");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__19 = RT.var((String)"clojure.set", (String)"intersection");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__21 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__23 = 2L;
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"interleave");
    public static final Var const__25 = RT.var((String)"datomic.measure.query-stats", (String)"acc-with-phase-stats!");
    public static final Keyword const__26 = RT.keyword(null, (String)"sched");

    public static Object invokeStatic(Object srcs, Object prog, Object p__18454, Object init_binds) {
        Object npreds;
        datalog$sched_in_order$pred_QMARK___18514 pred_QMARK_;
        datalog$sched_in_order$in_clause_QMARK___18495 in_clause_QMARK_;
        Object vec__18458;
        Object object = p__18454;
        p__18454 = null;
        Object vec__18455 = object;
        Object seq__18456 = ((IFn)const__0.getRawRoot()).invoke(vec__18455);
        Object first__18457 = ((IFn)const__1.getRawRoot()).invoke(seq__18456);
        Object object2 = seq__18456;
        seq__18456 = null;
        Object seq__184562 = ((IFn)const__2.getRawRoot()).invoke(object2);
        Object object3 = first__18457;
        first__18457 = null;
        Object object4 = vec__18458 = object3;
        vec__18458 = null;
        Object seq__18459 = ((IFn)const__0.getRawRoot()).invoke(object4);
        Object first__18460 = ((IFn)const__1.getRawRoot()).invoke(seq__18459);
        Object object5 = seq__18459;
        seq__18459 = null;
        Object seq__184592 = ((IFn)const__2.getRawRoot()).invoke(object5);
        Object object6 = first__18460;
        first__18460 = null;
        Object hpred = object6;
        Object object7 = seq__184592;
        seq__184592 = null;
        Object hargs = object7;
        Object object8 = seq__184562;
        seq__184562 = null;
        Object body = object8;
        vec__18455 = null;
        datalog$sched_in_order$src__18470 src = new datalog$sched_in_order$src__18470();
        datalog$sched_in_order$pack__18473 pack2 = new datalog$sched_in_order$pack__18473((Object)src);
        datalog$sched_in_order$unpack__18475 unpack2 = new datalog$sched_in_order$unpack__18475();
        datalog$sched_in_order$cargs__18477 cargs = new datalog$sched_in_order$cargs__18477(prog);
        Object object9 = srcs;
        srcs = null;
        datalog$sched_in_order$extdb__18480 extdb = new datalog$sched_in_order$extdb__18480(prog, object9);
        datalog$sched_in_order$cbinds__18489 cbinds2 = new datalog$sched_in_order$cbinds__18489();
        datalog$sched_in_order$sv_clause_QMARK___18491 sv_clause_QMARK_ = new datalog$sched_in_order$sv_clause_QMARK___18491();
        datalog$sched_in_order$src__18470 datalog$sched_in_order$src__18470 = src;
        src = null;
        datalog$sched_in_order$in_clause_QMARK___18495 datalog$sched_in_order$in_clause_QMARK___18495 = in_clause_QMARK_ = new datalog$sched_in_order$in_clause_QMARK___18495((Object)datalog$sched_in_order$src__18470);
        in_clause_QMARK_ = null;
        datalog$sched_in_order$delay_ins__18500 delay_ins = new datalog$sched_in_order$delay_ins__18500((Object)cbinds2, (Object)datalog$sched_in_order$in_clause_QMARK___18495, (Object)cargs);
        datalog$sched_in_order$pred_QMARK___18514 datalog$sched_in_order$pred_QMARK___18514 = pred_QMARK_ = new datalog$sched_in_order$pred_QMARK___18514();
        pred_QMARK_ = null;
        Object object10 = body;
        body = null;
        Object vec__18461 = ((IFn)const__3.getRawRoot()).invoke((Object)datalog$sched_in_order$pred_QMARK___18514, object10);
        Object preds = RT.nth((Object)vec__18461, (int)RT.uncheckedIntCast((long)0L), null);
        Object object11 = vec__18461;
        vec__18461 = null;
        Object object12 = npreds = RT.nth((Object)object11, (int)RT.uncheckedIntCast((long)1L), null);
        npreds = null;
        Object vec__18464 = ((IFn)const__3.getRawRoot()).invoke(const__7.getRawRoot(), object12);
        Object njcs = RT.nth((Object)vec__18464, (int)RT.uncheckedIntCast((long)0L), null);
        Object object13 = vec__18464;
        vec__18464 = null;
        Object npreds2 = RT.nth((Object)object13, (int)RT.uncheckedIntCast((long)1L), null);
        datalog$sched_in_order$sv_clause_QMARK___18491 datalog$sched_in_order$sv_clause_QMARK___18491 = sv_clause_QMARK_;
        sv_clause_QMARK_ = null;
        Object object14 = npreds2;
        npreds2 = null;
        Object vec__18467 = ((IFn)const__3.getRawRoot()).invoke((Object)datalog$sched_in_order$sv_clause_QMARK___18491, object14);
        Object svs = RT.nth((Object)vec__18467, (int)RT.uncheckedIntCast((long)0L), null);
        Object object15 = vec__18467;
        vec__18467 = null;
        Object npreds3 = RT.nth((Object)object15, (int)RT.uncheckedIntCast((long)1L), null);
        Object object16 = svs;
        svs = null;
        Object object17 = preds;
        preds = null;
        Object object18 = njcs;
        njcs = null;
        datalog$sched_in_order$delay_ins__18500 datalog$sched_in_order$delay_ins__18500 = delay_ins;
        delay_ins = null;
        Object object19 = npreds3;
        npreds3 = null;
        Object body2 = ((IFn)const__8.getRawRoot()).invoke(object16, object17, object18, ((IFn)datalog$sched_in_order$delay_ins__18500).invoke(object19));
        datalog$sched_in_order$reqcnt__18517 reqcnt = new datalog$sched_in_order$reqcnt__18517(prog);
        datalog$sched_in_order$extdb__18480 datalog$sched_in_order$extdb__18480 = extdb;
        extdb = null;
        Object object20 = prog;
        prog = null;
        Object object21 = hpred;
        hpred = null;
        datalog$sched_in_order$cargs__18477 datalog$sched_in_order$cargs__18477 = cargs;
        cargs = null;
        datalog$sched_in_order$underbound_QMARK___18522 underbound_QMARK_ = new datalog$sched_in_order$underbound_QMARK___18522((Object)unpack2, (Object)datalog$sched_in_order$extdb__18480, (Object)reqcnt, object20, object21, (Object)datalog$sched_in_order$cargs__18477);
        datalog$sched_in_order$unpack__18475 datalog$sched_in_order$unpack__18475 = unpack2;
        unpack2 = null;
        datalog$sched_in_order$underbound_QMARK___18522 datalog$sched_in_order$underbound_QMARK___18522 = underbound_QMARK_;
        underbound_QMARK_ = null;
        datalog$sched_in_order$reqcnt__18517 datalog$sched_in_order$reqcnt__18517 = reqcnt;
        reqcnt = null;
        datalog$sched_in_order$pack__18473 datalog$sched_in_order$pack__18473 = pack2;
        pack2 = null;
        Object object22 = body2;
        body2 = null;
        Object clauses = ((IFn)new datalog$sched_in_order$fn__18539(init_binds, (Object)datalog$sched_in_order$unpack__18475, (Object)datalog$sched_in_order$underbound_QMARK___18522, (Object)datalog$sched_in_order$reqcnt__18517, (Object)cbinds2, (Object)datalog$sched_in_order$pack__18473, object22)).invoke();
        datalog$sched_in_order$cbinds__18489 datalog$sched_in_order$cbinds__18489 = cbinds2;
        cbinds2 = null;
        Object object23 = init_binds;
        init_binds = null;
        Object blist = ((IFn)const__9.getRawRoot()).invoke((Object)new datalog$sched_in_order$fn__18547((Object)datalog$sched_in_order$cbinds__18489), ((IFn)const__10.getRawRoot()).invoke(object23, ((IFn)const__11.getRawRoot()).invoke(clauses, hargs)));
        Object bup = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), blist));
        Object object24 = blist;
        blist = null;
        Object bdown = ((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(object24))));
        Object object25 = bup;
        bup = null;
        Object object26 = bdown;
        bdown = null;
        Object object27 = hargs;
        hargs = null;
        Object binds = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke(const__17.getRawRoot(), const__19.getRawRoot()), object25, object26))), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__20.getRawRoot()).invoke(const__21.getRawRoot(), object27)));
        Object object28 = clauses;
        clauses = null;
        Object object29 = binds;
        binds = null;
        Object ret = ((IFn)const__22.getRawRoot()).invoke(const__23, ((IFn)const__24.getRawRoot()).invoke(object28, object29));
        ((IFn)const__25.getRawRoot()).invoke((Object)const__26, ret);
        Object object30 = ret;
        ret = null;
        return object30;
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
        return datalog$sched_in_order.invokeStatic(object5, object6, object7, object8);
    }
}

