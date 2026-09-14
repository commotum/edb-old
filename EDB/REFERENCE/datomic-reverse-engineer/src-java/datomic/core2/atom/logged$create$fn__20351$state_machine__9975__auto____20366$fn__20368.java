/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class logged$create$fn__20351$state_machine__9975__auto____20366$fn__20368
extends AFunction {
    Object G__20328;
    Object G__20326;
    Object G__20327;
    Object G__20325;
    Object G__20322;
    Object G__20324;
    Object G__20323;
    Object state_20350;
    Object old_frame__9976__auto__;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Var const__12 = RT.var((String)"datomic.core2.atom.logged", (String)"append-value");
    public static final Var const__13 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__14 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__15 = 2L;
    public static final Var const__17 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Var const__18 = RT.var((String)"datomic.core2.atom.logged", (String)"create*");
    public static final Keyword const__19 = RT.keyword(null, (String)"header");
    public static final Keyword const__20 = RT.keyword(null, (String)"value");
    public static final Var const__21 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__24 = RT.keyword(null, (String)"recur");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"first");

    public logged$create$fn__20351$state_machine__9975__auto____20366$fn__20368(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.G__20328 = object;
        this.G__20326 = object2;
        this.G__20327 = object3;
        this.G__20325 = object4;
        this.G__20322 = object5;
        this.G__20324 = object6;
        this.G__20323 = object7;
        this.state_20350 = object8;
        this.old_frame__9976__auto__ = object9;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__9978__auto__20375;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 3L));
            do {
                Object object2;
                int G__20369 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 1L));
                switch (G__20369) {
                    case 1: {
                        Object state_20350;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 6L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 7L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 8L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 9L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 10L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 11L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 12L);
                        Object inst_20330 = ((IFn)this.G__20322).invoke();
                        Object inst_20331 = ((IFn)this.G__20323).invoke();
                        Object inst_20332 = ((IFn)this.G__20324).invoke();
                        Object inst_20333 = ((IFn)this.G__20325).invoke();
                        Object inst_20334 = ((IFn)this.G__20326).invoke();
                        Object inst_20335 = ((IFn)this.G__20327).invoke();
                        Object inst_20336 = ((IFn)this.G__20328).invoke();
                        Object object3 = inst_20330;
                        inst_20330 = null;
                        Object inst_20337 = object3;
                        Object object4 = inst_20331;
                        inst_20331 = null;
                        Object inst_20338 = object4;
                        Object object5 = inst_20332;
                        inst_20332 = null;
                        Object inst_20339 = object5;
                        Object object6 = inst_20333;
                        inst_20333 = null;
                        Object inst_20340 = object6;
                        Object object7 = inst_20334;
                        inst_20334 = null;
                        Object inst_20341 = object7;
                        Object object8 = inst_20335;
                        inst_20335 = null;
                        Object inst_20342 = object8;
                        Object object9 = inst_20336;
                        inst_20336 = null;
                        Object inst_20343 = object9;
                        Object log2 = inst_20340;
                        Object serialize = inst_20343;
                        Object value = inst_20342;
                        Object header = inst_20341;
                        Object object10 = log2;
                        log2 = null;
                        Object object11 = serialize;
                        serialize = null;
                        Object object12 = header;
                        header = null;
                        Object object13 = value;
                        value = null;
                        Object inst_20344 = ((IFn)const__12.getRawRoot()).invoke(object10, object11, object12, object13);
                        Object statearr_20370 = this.state_20350;
                        Object object14 = inst_20337;
                        inst_20337 = null;
                        ((IFn.OLOO)const__13.getRawRoot()).invokePrim(statearr_20370, 7L, object14);
                        Object object15 = inst_20338;
                        inst_20338 = null;
                        ((IFn.OLOO)const__13.getRawRoot()).invokePrim(statearr_20370, 12L, object15);
                        Object object16 = inst_20339;
                        inst_20339 = null;
                        ((IFn.OLOO)const__13.getRawRoot()).invokePrim(statearr_20370, 8L, object16);
                        Object object17 = inst_20340;
                        inst_20340 = null;
                        ((IFn.OLOO)const__13.getRawRoot()).invokePrim(statearr_20370, 6L, object17);
                        Object object18 = inst_20341;
                        inst_20341 = null;
                        ((IFn.OLOO)const__13.getRawRoot()).invokePrim(statearr_20370, 11L, object18);
                        Object object19 = inst_20342;
                        inst_20342 = null;
                        ((IFn.OLOO)const__13.getRawRoot()).invokePrim(statearr_20370, 10L, object19);
                        Object object20 = inst_20343;
                        inst_20343 = null;
                        ((IFn.OLOO)const__13.getRawRoot()).invokePrim(statearr_20370, 9L, object20);
                        Object object21 = statearr_20370;
                        statearr_20370 = null;
                        Object object22 = state_20350 = object21;
                        state_20350 = null;
                        Object object23 = inst_20344;
                        inst_20344 = null;
                        object2 = ((IFn)const__14.getRawRoot()).invoke(object22, const__15, object23);
                        break;
                    }
                    case 2: {
                        Object inst_20348;
                        Object object24;
                        Object or__5581__auto__20374;
                        Object inst_20346;
                        Object inst_20340 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 6L);
                        Object inst_20337 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 7L);
                        Object inst_20339 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 8L);
                        Object inst_20343 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 9L);
                        Object inst_20342 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 10L);
                        Object inst_20341 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 11L);
                        Object inst_20338 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 12L);
                        Object object25 = inst_20346 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 2L);
                        inst_20346 = null;
                        Object inst_20347 = object25;
                        inst_20340 = null;
                        inst_20337 = null;
                        Object object26 = inst_20339;
                        inst_20339 = null;
                        Object args = object26;
                        inst_20343 = null;
                        Object object27 = inst_20342;
                        inst_20342 = null;
                        Object value = object27;
                        Object object28 = inst_20347;
                        inst_20347 = null;
                        Object loaded = object28;
                        Object object29 = inst_20341;
                        inst_20341 = null;
                        Object header = object29;
                        inst_20338 = null;
                        Object object30 = loaded;
                        loaded = null;
                        Object object31 = or__5581__auto__20374 = ((IFn)const__17.getRawRoot()).invoke(object30);
                        if (object31 != null && object31 != Boolean.FALSE) {
                            object24 = or__5581__auto__20374;
                            or__5581__auto__20374 = null;
                        } else {
                            Object object32 = args;
                            args = null;
                            Object[] objectArray = new Object[4];
                            objectArray[0] = const__19;
                            Object object33 = header;
                            header = null;
                            objectArray[1] = object33;
                            objectArray[2] = const__20;
                            Object object34 = value;
                            value = null;
                            objectArray[3] = object34;
                            object24 = ((IFn)const__18.getRawRoot()).invoke(object32, (Object)RT.mapUniqueKeys((Object[])objectArray));
                        }
                        Object object35 = inst_20348 = object24;
                        inst_20348 = null;
                        object2 = ((IFn)const__21.getRawRoot()).invoke(this.state_20350, object35);
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__22.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__20369));
                    }
                }
                result__9978__auto__20375 = object2;
            } while (Util.identical((Object)result__9978__auto__20375, (Object)const__24));
            Object object36 = result__9978__auto__20375;
            result__9978__auto__20375 = null;
            object = object36;
        }
        catch (Throwable ex__9979__auto__2) {
            Object statearr_20371 = this.state_20350;
            ((IFn.OLOO)const__13.getRawRoot()).invokePrim(statearr_20371, 2L, (Object)ex__9979__auto__2);
            Object object37 = ((IFn)const__25.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 4L));
            if (object37 == null || object37 == Boolean.FALSE) {
                Object ex__9979__auto__2 = null;
                throw ex__9979__auto__2;
            }
            Object statearr_20372 = this.state_20350;
            ((IFn.OLOO)const__13.getRawRoot()).invokePrim(statearr_20372, 1L, ((IFn)const__27.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20350, 4L)));
            object = const__24;
        }
        finally {
            this.state_20350 = null;
            ((IFn.OLOO)const__13.getRawRoot()).invokePrim(this.state_20350, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return object;
    }
}

