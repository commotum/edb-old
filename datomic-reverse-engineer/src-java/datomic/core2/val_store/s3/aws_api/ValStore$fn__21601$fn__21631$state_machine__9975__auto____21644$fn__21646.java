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
package datomic.core2.val_store.s3.aws_api;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644$fn__21646
extends AFunction {
    Object G__21602;
    Object state_21630;
    Object old_frame__9976__auto__;
    Object G__21604;
    Object G__21607;
    Object G__21606;
    Object G__21603;
    Object G__21605;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Var const__11 = RT.var((String)"cognitect.aws.client.api", (String)"invoke-async");
    public static final Var const__12 = RT.var((String)"datomic.core2.aws.s3.aws-api", (String)"delete-object-request");
    public static final Keyword const__13 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__14 = RT.keyword(null, (String)"key");
    public static final Var const__15 = RT.var((String)"datomic.core2.val-store.s3", (String)"storage-key");
    public static final Var const__16 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__17 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__18 = 2L;
    public static final Var const__20 = RT.var((String)"datomic.core2.async", (String)"channel-closed-error");
    public static final Var const__21 = RT.var((String)"datomic.core2.anomalies", (String)"ok?");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"merge");
    public static final AFn const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"result"), RT.keyword(null, (String)"deleted")});
    public static final Var const__26 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__29 = RT.keyword(null, (String)"recur");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"first");

    public ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644$fn__21646(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.G__21602 = object;
        this.state_21630 = object2;
        this.old_frame__9976__auto__ = object3;
        this.G__21604 = object4;
        this.G__21607 = object5;
        this.G__21606 = object6;
        this.G__21603 = object7;
        this.G__21605 = object8;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__9978__auto__21654;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 3L));
            do {
                Object object2;
                int G__21647 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 1L));
                switch (G__21647) {
                    case 1: {
                        Object state_21630;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 6L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 7L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 8L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 9L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 10L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 11L);
                        Object inst_21610 = ((IFn)this.G__21602).invoke();
                        Object inst_21611 = ((IFn)this.G__21603).invoke();
                        Object inst_21612 = ((IFn)this.G__21604).invoke();
                        Object inst_21613 = ((IFn)this.G__21605).invoke();
                        Object inst_21614 = ((IFn)this.G__21606).invoke();
                        Object inst_21615 = ((IFn)this.G__21607).invoke();
                        Object object3 = inst_21610;
                        inst_21610 = null;
                        Object inst_21616 = object3;
                        Object object4 = inst_21611;
                        inst_21611 = null;
                        Object inst_21617 = object4;
                        Object object5 = inst_21612;
                        inst_21612 = null;
                        Object inst_21618 = object5;
                        Object object6 = inst_21613;
                        inst_21613 = null;
                        Object inst_21619 = object6;
                        Object object7 = inst_21614;
                        inst_21614 = null;
                        Object inst_21620 = object7;
                        Object object8 = inst_21615;
                        inst_21615 = null;
                        Object inst_21621 = object8;
                        Object bucket = inst_21616;
                        Object client2 = inst_21618;
                        Object k = inst_21619;
                        Object prefix = inst_21620;
                        Object opts = inst_21621;
                        Object object9 = client2;
                        client2 = null;
                        Object[] objectArray = new Object[4];
                        objectArray[0] = const__13;
                        Object object10 = bucket;
                        bucket = null;
                        objectArray[1] = object10;
                        objectArray[2] = const__14;
                        Object object11 = prefix;
                        prefix = null;
                        Object object12 = k;
                        k = null;
                        Object object13 = opts;
                        opts = null;
                        objectArray[3] = ((IFn)const__15.getRawRoot()).invoke(object11, object12, object13);
                        Object inst_21622 = ((IFn)const__11.getRawRoot()).invoke(object9, ((IFn)const__12.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                        Object statearr_21648 = this.state_21630;
                        Object object14 = inst_21616;
                        inst_21616 = null;
                        ((IFn.OLOO)const__16.getRawRoot()).invokePrim(statearr_21648, 6L, object14);
                        Object object15 = inst_21617;
                        inst_21617 = null;
                        ((IFn.OLOO)const__16.getRawRoot()).invokePrim(statearr_21648, 7L, object15);
                        Object object16 = inst_21618;
                        inst_21618 = null;
                        ((IFn.OLOO)const__16.getRawRoot()).invokePrim(statearr_21648, 8L, object16);
                        Object object17 = inst_21619;
                        inst_21619 = null;
                        ((IFn.OLOO)const__16.getRawRoot()).invokePrim(statearr_21648, 9L, object17);
                        Object object18 = inst_21620;
                        inst_21620 = null;
                        ((IFn.OLOO)const__16.getRawRoot()).invokePrim(statearr_21648, 10L, object18);
                        Object object19 = inst_21621;
                        inst_21621 = null;
                        ((IFn.OLOO)const__16.getRawRoot()).invokePrim(statearr_21648, 11L, object19);
                        Object object20 = statearr_21648;
                        statearr_21648 = null;
                        Object object21 = state_21630 = object20;
                        state_21630 = null;
                        Object object22 = inst_21622;
                        inst_21622 = null;
                        object2 = ((IFn)const__17.getRawRoot()).invoke(object21, const__18, object22);
                        break;
                    }
                    case 2: {
                        Object inst_21628;
                        Object object23;
                        Object inst_21626;
                        Object object24;
                        Object or__5581__auto__21652;
                        Object inst_21625;
                        Object inst_21624;
                        Object inst_21616 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 6L);
                        Object inst_21617 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 7L);
                        Object inst_21618 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 8L);
                        Object inst_21619 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 9L);
                        Object inst_21620 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 10L);
                        Object inst_21621 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 11L);
                        Object object25 = inst_21624 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 2L);
                        inst_21624 = null;
                        Object object26 = inst_21625 = object25;
                        inst_21625 = null;
                        Object v__19654__auto__21653 = object26;
                        Object object27 = or__5581__auto__21652 = ((IFn)const__20.getRawRoot()).invoke(v__19654__auto__21653);
                        if (object27 != null && object27 != Boolean.FALSE) {
                            object24 = or__5581__auto__21652;
                            or__5581__auto__21652 = null;
                        } else {
                            object24 = v__19654__auto__21653;
                            v__19654__auto__21653 = null;
                        }
                        Object object28 = inst_21626 = object24;
                        inst_21626 = null;
                        Object inst_21627 = object28;
                        inst_21616 = null;
                        Object object29 = inst_21627;
                        inst_21627 = null;
                        Object ret = object29;
                        inst_21617 = null;
                        inst_21618 = null;
                        inst_21619 = null;
                        inst_21620 = null;
                        inst_21621 = null;
                        Object G__21609 = ret;
                        Object object30 = ret;
                        ret = null;
                        Object object31 = ((IFn)const__21.getRawRoot()).invoke(object30);
                        if (object31 != null && object31 != Boolean.FALSE) {
                            Object object32 = G__21609;
                            G__21609 = null;
                            object23 = ((IFn)const__22.getRawRoot()).invoke(object32, (Object)const__25);
                        } else {
                            object23 = G__21609;
                            G__21609 = null;
                        }
                        Object object33 = inst_21628 = object23;
                        inst_21628 = null;
                        object2 = ((IFn)const__26.getRawRoot()).invoke(this.state_21630, object33);
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__27.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__21647));
                    }
                }
                result__9978__auto__21654 = object2;
            } while (Util.identical((Object)result__9978__auto__21654, (Object)const__29));
            Object object34 = result__9978__auto__21654;
            result__9978__auto__21654 = null;
            object = object34;
        }
        catch (Throwable ex__9979__auto__2) {
            Object statearr_21649 = this.state_21630;
            ((IFn.OLOO)const__16.getRawRoot()).invokePrim(statearr_21649, 2L, (Object)ex__9979__auto__2);
            Object object35 = ((IFn)const__30.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 4L));
            if (object35 == null || object35 == Boolean.FALSE) {
                Object ex__9979__auto__2 = null;
                throw ex__9979__auto__2;
            }
            Object statearr_21650 = this.state_21630;
            ((IFn.OLOO)const__16.getRawRoot()).invokePrim(statearr_21650, 1L, ((IFn)const__32.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_21630, 4L)));
            object = const__29;
        }
        finally {
            this.state_21630 = null;
            ((IFn.OLOO)const__16.getRawRoot()).invokePrim(this.state_21630, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return object;
    }
}

