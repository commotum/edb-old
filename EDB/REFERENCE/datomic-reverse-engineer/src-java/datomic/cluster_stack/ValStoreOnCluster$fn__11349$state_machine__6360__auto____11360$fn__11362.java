/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.cluster_stack;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360$fn__11362
extends AFunction {
    Object G__11331;
    Object G__11327;
    Object G__11329;
    Object old_frame__6361__auto__;
    Object G__11328;
    Object G__11330;
    Object state_11348;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Var const__6 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__11 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__12 = 2L;
    public static final Keyword const__15 = RT.keyword(null, (String)"ok");
    public static final AFn const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"result"), RT.keyword(null, (String)"deleted")});
    public static final Var const__19 = RT.var((String)"datomic.cluster-stack", (String)"result->anom");
    public static final Var const__20 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__23 = RT.keyword(null, (String)"recur");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"first");

    public ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360$fn__11362(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.G__11331 = object;
        this.G__11327 = object2;
        this.G__11329 = object3;
        this.old_frame__6361__auto__ = object4;
        this.G__11328 = object5;
        this.G__11330 = object6;
        this.state_11348 = object7;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__6363__auto__11368;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 3L));
            do {
                Object object2;
                int G__11363 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 1L));
                switch (G__11363) {
                    case 1: {
                        Object state_11348;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 6L);
                        Object inst_11333 = ((IFn)this.G__11327).invoke();
                        Object inst_11334 = ((IFn)this.G__11328).invoke();
                        Object inst_11335 = ((IFn)this.G__11329).invoke();
                        Object inst_11336 = ((IFn)this.G__11330).invoke();
                        Object inst_11337 = ((IFn)this.G__11331).invoke();
                        Object object3 = inst_11333;
                        inst_11333 = null;
                        Object inst_11338 = object3;
                        Object object4 = inst_11334;
                        inst_11334 = null;
                        Object inst_11339 = object4;
                        Object object5 = inst_11335;
                        inst_11335 = null;
                        Object inst_11340 = object5;
                        Object object6 = inst_11336;
                        inst_11336 = null;
                        Object inst_11341 = object6;
                        Object object7 = inst_11337;
                        inst_11337 = null;
                        Object inst_11342 = object7;
                        Object statearr_11364 = this.state_11348;
                        Object object8 = inst_11338;
                        inst_11338 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11364, 7L, object8);
                        Object object9 = inst_11339;
                        inst_11339 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11364, 8L, object9);
                        Object object10 = inst_11340;
                        inst_11340 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11364, 9L, object10);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11364, 6L, inst_11341);
                        Object object11 = inst_11342;
                        inst_11342 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11364, 10L, object11);
                        Object object12 = statearr_11364;
                        statearr_11364 = null;
                        Object object13 = state_11348 = object12;
                        state_11348 = null;
                        Object object14 = inst_11341;
                        inst_11341 = null;
                        object2 = ((IFn)const__11.getRawRoot()).invoke(object13, const__12, object14);
                        break;
                    }
                    case 2: {
                        AFn inst_11346;
                        Object object15;
                        Object inst_11344;
                        Object inst_11338 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 7L);
                        Object inst_11339 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 8L);
                        Object inst_11340 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 9L);
                        Object inst_11341 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 6L);
                        Object inst_11342 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 10L);
                        Object object16 = inst_11344 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 2L);
                        inst_11344 = null;
                        Object inst_11345 = object16;
                        inst_11338 = null;
                        inst_11339 = null;
                        inst_11340 = null;
                        inst_11341 = null;
                        Object object17 = inst_11345;
                        inst_11345 = null;
                        Object result2 = object17;
                        inst_11342 = null;
                        if (Util.equiv((Object)const__15, (Object)result2)) {
                            object15 = const__18;
                        } else {
                            Object object18 = result2;
                            result2 = null;
                            object15 = ((IFn)const__19.getRawRoot()).invoke(object18);
                        }
                        AFn aFn = inst_11346 = object15;
                        inst_11346 = null;
                        object2 = ((IFn)const__20.getRawRoot()).invoke(this.state_11348, (Object)aFn);
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__21.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__11363));
                    }
                }
                result__6363__auto__11368 = object2;
            } while (Util.identical((Object)result__6363__auto__11368, (Object)const__23));
            Object object19 = result__6363__auto__11368;
            result__6363__auto__11368 = null;
            object = object19;
        }
        catch (Throwable ex__6364__auto__2) {
            Object statearr_11365 = this.state_11348;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11365, 2L, (Object)ex__6364__auto__2);
            Object object20 = ((IFn)const__24.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 4L));
            if (object20 == null || object20 == Boolean.FALSE) {
                Object ex__6364__auto__2 = null;
                throw ex__6364__auto__2;
            }
            Object statearr_11366 = this.state_11348;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11366, 1L, ((IFn)const__26.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11348, 4L)));
            object = const__23;
        }
        finally {
            this.state_11348 = null;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(this.state_11348, 3L, Var.getThreadBindingFrame());
            this.old_frame__6361__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__6361__auto__);
        }
        return object;
    }
}

