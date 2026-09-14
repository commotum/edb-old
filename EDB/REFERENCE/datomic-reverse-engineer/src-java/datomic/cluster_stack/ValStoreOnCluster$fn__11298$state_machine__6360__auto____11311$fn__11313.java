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

public final class ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311$fn__11313
extends AFunction {
    Object G__11277;
    Object G__11273;
    Object G__11276;
    Object G__11278;
    Object G__11274;
    Object G__11275;
    Object state_11297;
    Object old_frame__6361__auto__;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Var const__6 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__12 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__13 = 2L;
    public static final Keyword const__16 = RT.keyword(null, (String)"created");
    public static final AFn const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"result"), RT.keyword(null, (String)"created")});
    public static final Var const__19 = RT.var((String)"datomic.cluster-stack", (String)"result->anom");
    public static final Var const__20 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__23 = RT.keyword(null, (String)"recur");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"first");

    public ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311$fn__11313(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.G__11277 = object;
        this.G__11273 = object2;
        this.G__11276 = object3;
        this.G__11278 = object4;
        this.G__11274 = object5;
        this.G__11275 = object6;
        this.state_11297 = object7;
        this.old_frame__6361__auto__ = object8;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__6363__auto__11319;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 3L));
            do {
                Object object2;
                int G__11314 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 1L));
                switch (G__11314) {
                    case 1: {
                        Object state_11297;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 6L);
                        Object inst_11280 = ((IFn)this.G__11273).invoke();
                        Object inst_11281 = ((IFn)this.G__11274).invoke();
                        Object inst_11282 = ((IFn)this.G__11275).invoke();
                        Object inst_11283 = ((IFn)this.G__11276).invoke();
                        Object inst_11284 = ((IFn)this.G__11277).invoke();
                        Object inst_11285 = ((IFn)this.G__11278).invoke();
                        Object object3 = inst_11280;
                        inst_11280 = null;
                        Object inst_11286 = object3;
                        Object object4 = inst_11281;
                        inst_11281 = null;
                        Object inst_11287 = object4;
                        Object object5 = inst_11282;
                        inst_11282 = null;
                        Object inst_11288 = object5;
                        Object object6 = inst_11283;
                        inst_11283 = null;
                        Object inst_11289 = object6;
                        Object object7 = inst_11284;
                        inst_11284 = null;
                        Object inst_11290 = object7;
                        Object object8 = inst_11285;
                        inst_11285 = null;
                        Object inst_11291 = object8;
                        Object statearr_11315 = this.state_11297;
                        Object object9 = inst_11286;
                        inst_11286 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11315, 7L, object9);
                        Object object10 = inst_11287;
                        inst_11287 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11315, 8L, object10);
                        Object object11 = inst_11288;
                        inst_11288 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11315, 9L, object11);
                        Object object12 = inst_11289;
                        inst_11289 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11315, 10L, object12);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11315, 6L, inst_11290);
                        Object object13 = inst_11291;
                        inst_11291 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11315, 11L, object13);
                        Object object14 = statearr_11315;
                        statearr_11315 = null;
                        Object object15 = state_11297 = object14;
                        state_11297 = null;
                        Object object16 = inst_11290;
                        inst_11290 = null;
                        object2 = ((IFn)const__12.getRawRoot()).invoke(object15, const__13, object16);
                        break;
                    }
                    case 2: {
                        AFn inst_11295;
                        Object object17;
                        Object inst_11293;
                        Object inst_11286 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 7L);
                        Object inst_11287 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 8L);
                        Object inst_11288 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 9L);
                        Object inst_11289 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 10L);
                        Object inst_11290 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 6L);
                        Object inst_11291 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 11L);
                        Object object18 = inst_11293 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 2L);
                        inst_11293 = null;
                        Object inst_11294 = object18;
                        inst_11286 = null;
                        inst_11287 = null;
                        inst_11288 = null;
                        inst_11289 = null;
                        inst_11290 = null;
                        Object object19 = inst_11294;
                        inst_11294 = null;
                        Object result2 = object19;
                        inst_11291 = null;
                        if (Util.equiv((Object)result2, (Object)const__16)) {
                            object17 = const__18;
                        } else {
                            Object object20 = result2;
                            result2 = null;
                            object17 = ((IFn)const__19.getRawRoot()).invoke(object20);
                        }
                        AFn aFn = inst_11295 = object17;
                        inst_11295 = null;
                        object2 = ((IFn)const__20.getRawRoot()).invoke(this.state_11297, (Object)aFn);
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__21.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__11314));
                    }
                }
                result__6363__auto__11319 = object2;
            } while (Util.identical((Object)result__6363__auto__11319, (Object)const__23));
            Object object21 = result__6363__auto__11319;
            result__6363__auto__11319 = null;
            object = object21;
        }
        catch (Throwable ex__6364__auto__2) {
            Object statearr_11316 = this.state_11297;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11316, 2L, (Object)ex__6364__auto__2);
            Object object22 = ((IFn)const__24.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 4L));
            if (object22 == null || object22 == Boolean.FALSE) {
                Object ex__6364__auto__2 = null;
                throw ex__6364__auto__2;
            }
            Object statearr_11317 = this.state_11297;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11317, 1L, ((IFn)const__26.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11297, 4L)));
            object = const__23;
        }
        finally {
            this.state_11297 = null;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(this.state_11297, 3L, Var.getThreadBindingFrame());
            this.old_frame__6361__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__6361__auto__);
        }
        return object;
    }
}

