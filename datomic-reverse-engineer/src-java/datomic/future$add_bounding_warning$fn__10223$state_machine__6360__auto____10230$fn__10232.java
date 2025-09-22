/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230$fn__10232
extends AFunction {
    Object G__10182;
    Object G__10181;
    Object G__10183;
    Object old_frame__6361__auto__;
    Object state_10222;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Object const__1 = 3L;
    public static final Object const__3 = 1L;
    public static final Var const__6 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__7 = 6L;
    public static final Object const__8 = 9L;
    public static final Object const__9 = 10L;
    public static final Object const__11 = 2L;
    public static final Keyword const__12 = RT.keyword(null, (String)"recur");
    public static final Object const__14 = 4L;
    public static final Object const__15 = 5L;
    public static final Var const__17 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__19 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__20 = RT.keyword(null, (String)"FutureBoundExceeded");
    public static final Var const__21 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__22 = RT.keyword(null, (String)"event");
    public static final Keyword const__23 = RT.keyword((String)"datomic.future", (String)"unfilled");
    public static final Keyword const__24 = RT.keyword(null, (String)"seconds");
    public static final Keyword const__25 = RT.keyword(null, (String)"context");
    public static final Var const__27 = RT.var((String)"clojure.core.async", (String)"timeout");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__30 = RT.var((String)"clojure.core.async", (String)"ioc-alts!");
    public static final Object const__31 = 7L;
    public static final Object const__34 = 8L;
    public static final Var const__38 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__40 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__41 = RT.var((String)"clojure.core", (String)"first");

    public future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230$fn__10232(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.G__10182 = object;
        this.G__10181 = object2;
        this.G__10183 = object3;
        this.old_frame__6361__auto__ = object4;
        this.state_10222 = object5;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__6363__auto__10250;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 3L));
            do {
                Object object2;
                int G__10233 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 1L));
                switch (G__10233) {
                    case 1: {
                        Object state_10222;
                        Object inst_10189 = ((IFn)this.G__10181).invoke();
                        Object inst_10190 = ((IFn)this.G__10182).invoke();
                        Object inst_10191 = ((IFn)this.G__10183).invoke();
                        Object object3 = inst_10189;
                        inst_10189 = null;
                        Object inst_10192 = object3;
                        Object object4 = inst_10190;
                        inst_10190 = null;
                        Object inst_10193 = object4;
                        Object object5 = inst_10191;
                        inst_10191 = null;
                        Object inst_10194 = object5;
                        long inst_10195 = 0L;
                        Object statearr_10234 = this.state_10222;
                        Object object6 = inst_10192;
                        inst_10192 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10234, 6L, object6);
                        Object object7 = inst_10193;
                        inst_10193 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10234, 9L, object7);
                        Object object8 = inst_10194;
                        inst_10194 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10234, 10L, object8);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10234, 11L, (Object)Numbers.num((long)inst_10195));
                        Object object9 = statearr_10234;
                        statearr_10234 = null;
                        Object object10 = state_10222 = object9;
                        state_10222 = null;
                        Object statearr_10235 = object10;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10235, 2L, null);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10235, 1L, const__11);
                        object2 = const__12;
                        break;
                    }
                    case 2: {
                        Object inst_10195 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 11L);
                        Object inst_10194 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 10L);
                        Object object11 = inst_10195;
                        inst_10195 = null;
                        Object object12 = inst_10194;
                        inst_10194 = null;
                        boolean inst_10197 = Util.equiv((Object)object11, (Object)object12);
                        if (inst_10197) {
                            Object statearr_10236 = this.state_10222;
                            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10236, 1L, const__14);
                        } else {
                            Object statearr_10237 = this.state_10222;
                            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10237, 1L, const__15);
                        }
                        object2 = const__12;
                        break;
                    }
                    case 3: {
                        Object inst_10220;
                        Object object13 = inst_10220 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 2L);
                        inst_10220 = null;
                        object2 = ((IFn)const__17.getRawRoot()).invoke(this.state_10222, object13);
                        break;
                    }
                    case 4: {
                        Object inst_10192 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 6L);
                        Object inst_10193 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 9L);
                        Object inst_10194 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 10L);
                        Object inst_10195 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 11L);
                        inst_10192 = null;
                        Object object14 = inst_10193;
                        inst_10193 = null;
                        Object context = object14;
                        Object object15 = inst_10194;
                        inst_10194 = null;
                        Object seconds = object15;
                        inst_10195 = null;
                        ((IFn)const__19.getRawRoot()).invoke((Object)const__20, const__3);
                        Logger logger = LoggerFactory.getLogger((String)"datomic.future");
                        if (logger.isWarnEnabled()) {
                            Logger logger2 = logger;
                            logger = null;
                            Object[] objectArray = new Object[6];
                            objectArray[0] = const__22;
                            objectArray[1] = const__23;
                            objectArray[2] = const__24;
                            Object object16 = seconds;
                            seconds = null;
                            objectArray[3] = object16;
                            objectArray[4] = const__25;
                            Object object17 = context;
                            context = null;
                            objectArray[5] = object17;
                            logger2.warn((String)((IFn)const__21.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                        }
                        Object inst_10199 = null;
                        Object statearr_10238 = this.state_10222;
                        Object v18 = inst_10199;
                        inst_10199 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10238, 2L, v18);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10238, 1L, const__7);
                        object2 = const__12;
                        break;
                    }
                    case 5: {
                        Object inst_10202;
                        Object inst_10192 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 6L);
                        Object inst_10201 = ((IFn.LO)const__27.getRawRoot()).invokePrim(1000L);
                        Object object18 = inst_10192;
                        inst_10192 = null;
                        Object object19 = inst_10201;
                        inst_10201 = null;
                        Object object20 = inst_10202 = ((IFn)const__29.getRawRoot()).invoke(object18, object19);
                        inst_10202 = null;
                        object2 = ((IFn)const__30.getRawRoot()).invoke(this.state_10222, const__31, object20);
                        break;
                    }
                    case 6: {
                        Object inst_10218 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 2L);
                        Object statearr_10239 = this.state_10222;
                        Object object21 = inst_10218;
                        inst_10218 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10239, 2L, object21);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10239, 1L, const__1);
                        object2 = const__12;
                        break;
                    }
                    case 7: {
                        Object inst_10209;
                        Object inst_10192 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 6L);
                        Object inst_10204 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 2L);
                        Object inst_10205 = RT.nth((Object)inst_10204, (int)RT.intCast((long)0L), null);
                        Object inst_10206 = RT.nth((Object)inst_10204, (int)RT.intCast((long)1L), null);
                        Object object22 = inst_10204;
                        inst_10204 = null;
                        Object inst_10207 = object22;
                        Object object23 = inst_10205;
                        inst_10205 = null;
                        Object inst_10208 = object23;
                        Object object24 = inst_10206;
                        inst_10206 = null;
                        Object object25 = inst_10209 = object24;
                        inst_10209 = null;
                        Object object26 = inst_10192;
                        inst_10192 = null;
                        boolean inst_10210 = Util.equiv((Object)object25, (Object)object26);
                        Object statearr_10240 = this.state_10222;
                        Object object27 = inst_10207;
                        inst_10207 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10240, 7L, object27);
                        Object object28 = inst_10208;
                        inst_10208 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10240, 8L, object28);
                        Object object29 = statearr_10240;
                        statearr_10240 = null;
                        Object state_10222 = object29;
                        if (inst_10210) {
                            Object object30 = state_10222;
                            state_10222 = null;
                            Object statearr_10241 = object30;
                            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10241, 1L, const__34);
                        } else {
                            Object object31 = state_10222;
                            state_10222 = null;
                            Object statearr_10242 = object31;
                            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10242, 1L, const__8);
                        }
                        object2 = const__12;
                        break;
                    }
                    case 8: {
                        Object statearr_10243 = this.state_10222;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10243, 2L, null);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10243, 1L, const__9);
                        object2 = const__12;
                        break;
                    }
                    case 9: {
                        Object state_10222;
                        Number inst_10213;
                        Object inst_10195;
                        Object object32 = inst_10195 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 11L);
                        inst_10195 = null;
                        Number number = inst_10213 = Numbers.inc((Object)object32);
                        inst_10213 = null;
                        Number inst_101952 = number;
                        Object statearr_10244 = this.state_10222;
                        Number number2 = inst_101952;
                        inst_101952 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10244, 11L, (Object)number2);
                        Object object33 = statearr_10244;
                        statearr_10244 = null;
                        Object object34 = state_10222 = object33;
                        state_10222 = null;
                        Object statearr_10245 = object34;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10245, 2L, null);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10245, 1L, const__11);
                        object2 = const__12;
                        break;
                    }
                    case 10: {
                        Object inst_10216 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 2L);
                        Object statearr_10246 = this.state_10222;
                        Object object35 = inst_10216;
                        inst_10216 = null;
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10246, 2L, object35);
                        ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10246, 1L, const__7);
                        object2 = const__12;
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__38.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__10233));
                    }
                }
                result__6363__auto__10250 = object2;
            } while (Util.identical((Object)result__6363__auto__10250, (Object)const__12));
            Object object36 = result__6363__auto__10250;
            result__6363__auto__10250 = null;
            object = object36;
        }
        catch (Throwable ex__6364__auto__2) {
            Object statearr_10247 = this.state_10222;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10247, 2L, (Object)ex__6364__auto__2);
            Object object37 = ((IFn)const__40.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 4L));
            if (object37 == null || object37 == Boolean.FALSE) {
                Object ex__6364__auto__2 = null;
                throw ex__6364__auto__2;
            }
            Object statearr_10248 = this.state_10222;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(statearr_10248, 1L, ((IFn)const__41.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_10222, 4L)));
            object = const__12;
        }
        finally {
            this.state_10222 = null;
            ((IFn.OLOO)const__6.getRawRoot()).invokePrim(this.state_10222, 3L, Var.getThreadBindingFrame());
            this.old_frame__6361__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__6361__auto__);
        }
        return object;
    }
}

