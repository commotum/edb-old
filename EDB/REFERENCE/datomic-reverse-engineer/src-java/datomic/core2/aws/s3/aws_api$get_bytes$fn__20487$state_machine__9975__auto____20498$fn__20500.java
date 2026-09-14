/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.aws.s3.aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498$fn__20500$fn__20503;

public final class aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498$fn__20500
extends AFunction {
    Object old_frame__9976__auto__;
    Object state_20486;
    Object G__20462;
    Object G__20464;
    Object G__20463;
    Object G__20465;
    Object G__20466;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Var const__10 = RT.var((String)"cognitect.aws.client.api", (String)"invoke-async");
    public static final Var const__11 = RT.var((String)"datomic.core2.aws.s3.aws-api", (String)"get-object-request");
    public static final Keyword const__12 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__13 = RT.keyword(null, (String)"key");
    public static final Var const__14 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__15 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__16 = 2L;
    public static final Var const__18 = RT.var((String)"datomic.core2.async", (String)"channel-closed-error");
    public static final Var const__19 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Var const__21 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__24 = RT.keyword(null, (String)"recur");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"first");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"Body"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498$fn__20500(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.old_frame__9976__auto__ = object;
        this.state_20486 = object2;
        this.G__20462 = object3;
        this.G__20464 = object4;
        this.G__20463 = object5;
        this.G__20465 = object6;
        this.G__20466 = object7;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__9978__auto__20511;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 3L));
            do {
                Object object2;
                int G__20501 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 1L));
                switch (G__20501) {
                    case 1: {
                        Object state_20486;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 6L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 7L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 8L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 9L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 10L);
                        Object inst_20468 = ((IFn)this.G__20462).invoke();
                        Object inst_20469 = ((IFn)this.G__20463).invoke();
                        Object inst_20470 = ((IFn)this.G__20464).invoke();
                        Object inst_20471 = ((IFn)this.G__20465).invoke();
                        Object inst_20472 = ((IFn)this.G__20466).invoke();
                        Object object3 = inst_20468;
                        inst_20468 = null;
                        Object inst_20473 = object3;
                        Object object4 = inst_20469;
                        inst_20469 = null;
                        Object inst_20474 = object4;
                        Object object5 = inst_20470;
                        inst_20470 = null;
                        Object inst_20475 = object5;
                        Object object6 = inst_20471;
                        inst_20471 = null;
                        Object inst_20476 = object6;
                        Object object7 = inst_20472;
                        inst_20472 = null;
                        Object inst_20477 = object7;
                        Object bucket = inst_20475;
                        Object key = inst_20477;
                        Object client2 = inst_20476;
                        Object object8 = client2;
                        client2 = null;
                        Object[] objectArray = new Object[4];
                        objectArray[0] = const__12;
                        Object object9 = bucket;
                        bucket = null;
                        objectArray[1] = object9;
                        objectArray[2] = const__13;
                        Object object10 = key;
                        key = null;
                        objectArray[3] = object10;
                        Object inst_20478 = ((IFn)const__10.getRawRoot()).invoke(object8, ((IFn)const__11.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                        Object statearr_20502 = this.state_20486;
                        Object object11 = inst_20473;
                        inst_20473 = null;
                        ((IFn.OLOO)const__14.getRawRoot()).invokePrim(statearr_20502, 10L, object11);
                        Object object12 = inst_20474;
                        inst_20474 = null;
                        ((IFn.OLOO)const__14.getRawRoot()).invokePrim(statearr_20502, 6L, object12);
                        Object object13 = inst_20475;
                        inst_20475 = null;
                        ((IFn.OLOO)const__14.getRawRoot()).invokePrim(statearr_20502, 7L, object13);
                        Object object14 = inst_20476;
                        inst_20476 = null;
                        ((IFn.OLOO)const__14.getRawRoot()).invokePrim(statearr_20502, 9L, object14);
                        Object object15 = inst_20477;
                        inst_20477 = null;
                        ((IFn.OLOO)const__14.getRawRoot()).invokePrim(statearr_20502, 8L, object15);
                        Object object16 = statearr_20502;
                        statearr_20502 = null;
                        Object object17 = state_20486 = object16;
                        state_20486 = null;
                        Object object18 = inst_20478;
                        inst_20478 = null;
                        object2 = ((IFn)const__15.getRawRoot()).invoke(object17, const__16, object18);
                        break;
                    }
                    case 2: {
                        Object inst_20484;
                        Object object19;
                        Object or__5581__auto__20509;
                        Object inst_20482;
                        Object object20;
                        Object or__5581__auto__20508;
                        Object inst_20480;
                        Object inst_20474 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 6L);
                        Object inst_20475 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 7L);
                        Object inst_20477 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 8L);
                        Object inst_20476 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 9L);
                        Object inst_20473 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 10L);
                        Object object21 = inst_20480 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 2L);
                        inst_20480 = null;
                        Object inst_20481 = object21;
                        Object object22 = inst_20481;
                        inst_20481 = null;
                        Object v__19654__auto__20510 = object22;
                        Object object23 = or__5581__auto__20508 = ((IFn)const__18.getRawRoot()).invoke(v__19654__auto__20510);
                        if (object23 != null && object23 != Boolean.FALSE) {
                            object20 = or__5581__auto__20508;
                            or__5581__auto__20508 = null;
                        } else {
                            object20 = v__19654__auto__20510;
                            v__19654__auto__20510 = null;
                        }
                        Object object24 = inst_20482 = object20;
                        inst_20482 = null;
                        Object inst_20483 = object24;
                        inst_20474 = null;
                        inst_20475 = null;
                        inst_20477 = null;
                        inst_20476 = null;
                        inst_20473 = null;
                        Object object25 = inst_20483;
                        inst_20483 = null;
                        Object result2 = object25;
                        Object object26 = or__5581__auto__20509 = ((IFn)const__19.getRawRoot()).invoke(result2);
                        if (object26 != null && object26 != Boolean.FALSE) {
                            object19 = or__5581__auto__20509;
                            or__5581__auto__20509 = null;
                        } else {
                            Object is;
                            ILookupThunk iLookupThunk = __thunk__0__;
                            Object object27 = result2;
                            Object object28 = iLookupThunk.get(object27);
                            if (iLookupThunk == object28) {
                                __thunk__0__ = __site__0__.fault(object27);
                                object28 = __thunk__0__.get(object27);
                            }
                            Object object29 = is = object28;
                            is = null;
                            Object object30 = result2;
                            result2 = null;
                            object19 = ((IFn)new aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498$fn__20500$fn__20503(object29, object30)).invoke();
                        }
                        Object object31 = inst_20484 = object19;
                        inst_20484 = null;
                        object2 = ((IFn)const__21.getRawRoot()).invoke(this.state_20486, object31);
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__22.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__20501));
                    }
                }
                result__9978__auto__20511 = object2;
            } while (Util.identical((Object)result__9978__auto__20511, (Object)const__24));
            Object object32 = result__9978__auto__20511;
            result__9978__auto__20511 = null;
            object = object32;
        }
        catch (Throwable ex__9979__auto__2) {
            Object statearr_20505 = this.state_20486;
            ((IFn.OLOO)const__14.getRawRoot()).invokePrim(statearr_20505, 2L, (Object)ex__9979__auto__2);
            Object object33 = ((IFn)const__25.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 4L));
            if (object33 == null || object33 == Boolean.FALSE) {
                Object ex__9979__auto__2 = null;
                throw ex__9979__auto__2;
            }
            Object statearr_20506 = this.state_20486;
            ((IFn.OLOO)const__14.getRawRoot()).invokePrim(statearr_20506, 1L, ((IFn)const__27.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20486, 4L)));
            object = const__24;
        }
        finally {
            this.state_20486 = null;
            ((IFn.OLOO)const__14.getRawRoot()).invokePrim(this.state_20486, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return object;
    }
}

