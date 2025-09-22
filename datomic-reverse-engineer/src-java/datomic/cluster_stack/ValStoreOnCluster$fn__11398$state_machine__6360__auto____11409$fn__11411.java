/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.cluster_stack;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409$fn__11411
extends AFunction {
    Object old_frame__6361__auto__;
    Object G__11379;
    Object G__11378;
    Object G__11376;
    Object state_11397;
    Object G__11377;
    Object G__11380;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Var const__6 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__11 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__12 = 2L;
    public static final Keyword const__15 = RT.keyword((String)"datomic.future", (String)"nil");
    public static final Keyword const__16 = RT.keyword(null, (String)"val");
    public static final AFn const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"val"), null});
    public static final Keyword const__19 = RT.keyword(null, (String)"else");
    public static final Var const__20 = RT.var((String)"datomic.cluster-stack", (String)"result->anom");
    public static final Var const__21 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__24 = RT.keyword(null, (String)"recur");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"first");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409$fn__11411(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.old_frame__6361__auto__ = object;
        this.G__11379 = object2;
        this.G__11378 = object3;
        this.G__11376 = object4;
        this.state_11397 = object5;
        this.G__11377 = object6;
        this.G__11380 = object7;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__6363__auto__11417;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 3L));
            do {
                Object object2;
                int G__11412 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 1L));
                switch (G__11412) {
                    case 1: {
                        Object state_11397;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 6L);
                        Object inst_11382 = ((IFn)this.G__11376).invoke();
                        Object inst_11383 = ((IFn)this.G__11377).invoke();
                        Object inst_11384 = ((IFn)this.G__11378).invoke();
                        Object inst_11385 = ((IFn)this.G__11379).invoke();
                        Object inst_11386 = ((IFn)this.G__11380).invoke();
                        Object object3 = inst_11382;
                        inst_11382 = null;
                        Object inst_11387 = object3;
                        Object object4 = inst_11383;
                        inst_11383 = null;
                        Object inst_11388 = object4;
                        Object object5 = inst_11384;
                        inst_11384 = null;
                        Object inst_11389 = object5;
                        Object object6 = inst_11385;
                        inst_11385 = null;
                        Object inst_11390 = object6;
                        Object object7 = inst_11386;
                        inst_11386 = null;
                        Object inst_11391 = object7;
                        Object statearr_11413 = this.state_11397;
                        Object object8 = inst_11387;
                        inst_11387 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11413, 7L, object8);
                        Object object9 = inst_11388;
                        inst_11388 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11413, 8L, object9);
                        Object object10 = inst_11389;
                        inst_11389 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11413, 9L, object10);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11413, 6L, inst_11390);
                        Object object11 = inst_11391;
                        inst_11391 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11413, 10L, object11);
                        Object object12 = statearr_11413;
                        statearr_11413 = null;
                        Object object13 = state_11397 = object12;
                        state_11397 = null;
                        Object object14 = inst_11390;
                        inst_11390 = null;
                        object2 = ((IFn)const__11.getRawRoot()).invoke(object13, const__12, object14);
                        break;
                    }
                    case 2: {
                        AFn inst_11395;
                        Object object15;
                        Object inst_11393;
                        Object inst_11387 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 7L);
                        Object inst_11388 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 8L);
                        Object inst_11389 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 9L);
                        Object inst_11390 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 6L);
                        Object inst_11391 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 10L);
                        Object object16 = inst_11393 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 2L);
                        inst_11393 = null;
                        Object inst_11394 = object16;
                        inst_11387 = null;
                        inst_11388 = null;
                        inst_11389 = null;
                        inst_11390 = null;
                        Object object17 = inst_11394;
                        inst_11394 = null;
                        Object result2 = object17;
                        inst_11391 = null;
                        if (Util.equiv((Object)const__15, (Object)result2)) {
                            object15 = const__17;
                        } else {
                            ILookupThunk iLookupThunk = __thunk__0__;
                            Object object18 = result2;
                            Object object19 = iLookupThunk.get(object18);
                            if (iLookupThunk == object19) {
                                __thunk__0__ = __site__0__.fault(object18);
                                object19 = __thunk__0__.get(object18);
                            }
                            if (object19 != null && object19 != Boolean.FALSE) {
                                Object[] objectArray = new Object[2];
                                objectArray[0] = const__16;
                                ILookupThunk iLookupThunk2 = __thunk__1__;
                                Object object20 = result2;
                                result2 = null;
                                Object object21 = iLookupThunk2.get(object20);
                                if (iLookupThunk2 == object21) {
                                    __thunk__1__ = __site__1__.fault(object20);
                                    object21 = __thunk__1__.get(object20);
                                }
                                objectArray[1] = object21;
                                object15 = RT.mapUniqueKeys((Object[])objectArray);
                            } else {
                                Keyword keyword = const__19;
                                if (keyword != null && keyword != Boolean.FALSE) {
                                    Object object22 = result2;
                                    result2 = null;
                                    object15 = ((IFn)const__20.getRawRoot()).invoke(object22);
                                } else {
                                    object15 = null;
                                }
                            }
                        }
                        AFn aFn = inst_11395 = object15;
                        inst_11395 = null;
                        object2 = ((IFn)const__21.getRawRoot()).invoke(this.state_11397, (Object)aFn);
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__22.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__11412));
                    }
                }
                result__6363__auto__11417 = object2;
            } while (Util.identical((Object)result__6363__auto__11417, (Object)const__24));
            Object object23 = result__6363__auto__11417;
            result__6363__auto__11417 = null;
            object = object23;
        }
        catch (Throwable ex__6364__auto__2) {
            Object statearr_11414 = this.state_11397;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11414, 2L, (Object)ex__6364__auto__2);
            Object object24 = ((IFn)const__25.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 4L));
            if (object24 == null || object24 == Boolean.FALSE) {
                Object ex__6364__auto__2 = null;
                throw ex__6364__auto__2;
            }
            Object statearr_11415 = this.state_11397;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_11415, 1L, ((IFn)const__27.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_11397, 4L)));
            object = const__24;
        }
        finally {
            this.state_11397 = null;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(this.state_11397, 3L, Var.getThreadBindingFrame());
            this.old_frame__6361__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__6361__auto__);
        }
        return object;
    }
}

