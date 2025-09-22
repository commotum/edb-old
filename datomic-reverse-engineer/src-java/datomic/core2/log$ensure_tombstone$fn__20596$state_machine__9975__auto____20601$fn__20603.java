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
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601$fn__20603
extends AFunction {
    Object G__20566;
    Object G__20565;
    Object state_20595;
    Object old_frame__9976__auto__;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Object const__1 = 3L;
    public static final Object const__5 = 6L;
    public static final Object const__6 = 7L;
    public static final Var const__7 = RT.var((String)"datomic.core2.log", (String)"scan");
    public static final AFn const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"direction"), RT.keyword(null, (String)"backward"), RT.keyword(null, (String)"limit"), 1L});
    public static final Var const__12 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__13 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__14 = 2L;
    public static final Object const__16 = 8L;
    public static final Object const__17 = 9L;
    public static final Var const__18 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Object const__19 = 4L;
    public static final Keyword const__20 = RT.keyword(null, (String)"recur");
    public static final Object const__22 = 5L;
    public static final Var const__25 = RT.var((String)"datomic.core2.log", (String)"item-header");
    public static final Keyword const__26 = RT.keyword(null, (String)"tombstone");
    public static final Var const__28 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__32 = RT.keyword(null, (String)"t");
    public static final Var const__34 = RT.var((String)"datomic.core2.log", (String)"append");
    public static final Var const__37 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__39 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__40 = RT.var((String)"clojure.core", (String)"first");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tombstone"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"next-t"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601$fn__20603(Object object, Object object2, Object object3, Object object4) {
        this.G__20566 = object;
        this.G__20565 = object2;
        this.state_20595 = object3;
        this.old_frame__9976__auto__ = object4;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__9978__auto__20623;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 3L));
            do {
                Object object2;
                int G__20604 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 1L));
                switch (G__20604) {
                    case 1: {
                        Object state_20595;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 6L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 7L);
                        Object inst_20568 = ((IFn)this.G__20565).invoke();
                        Object inst_20569 = ((IFn)this.G__20566).invoke();
                        Object object3 = inst_20568;
                        inst_20568 = null;
                        Object inst_20570 = object3;
                        Object object4 = inst_20569;
                        inst_20569 = null;
                        Object inst_20571 = object4;
                        Object log2 = inst_20570;
                        Object object5 = log2;
                        log2 = null;
                        Object inst_20572 = ((IFn)const__7.getRawRoot()).invoke(object5, (Object)const__11);
                        Object statearr_20605 = this.state_20595;
                        Object object6 = inst_20570;
                        inst_20570 = null;
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20605, 6L, object6);
                        Object object7 = inst_20571;
                        inst_20571 = null;
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20605, 7L, object7);
                        Object object8 = statearr_20605;
                        statearr_20605 = null;
                        Object object9 = state_20595 = object8;
                        state_20595 = null;
                        Object object10 = inst_20572;
                        inst_20572 = null;
                        object2 = ((IFn)const__13.getRawRoot()).invoke(object9, const__14, object10);
                        break;
                    }
                    case 2: {
                        Object inst_20576;
                        Object item;
                        Object inst_20574;
                        Object inst_20570 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 6L);
                        Object inst_20571 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 7L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 8L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 9L);
                        Object object11 = inst_20574 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 2L);
                        inst_20574 = null;
                        Object inst_20575 = object11;
                        inst_20570 = null;
                        inst_20571 = null;
                        Object object12 = item = inst_20575;
                        item = null;
                        Object object13 = inst_20576 = ((IFn)const__18.getRawRoot()).invoke(object12);
                        inst_20576 = null;
                        Object inst_20577 = object13;
                        Object statearr_20606 = this.state_20595;
                        Object object14 = inst_20575;
                        inst_20575 = null;
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20606, 8L, object14);
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20606, 9L, inst_20577);
                        Object object15 = statearr_20606;
                        statearr_20606 = null;
                        Object state_20595 = object15;
                        Object object16 = inst_20577;
                        inst_20577 = null;
                        if (object16 != null && object16 != Boolean.FALSE) {
                            Object object17 = state_20595;
                            state_20595 = null;
                            Object statearr_20607 = object17;
                            ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20607, 1L, const__1);
                        } else {
                            Object object18 = state_20595;
                            state_20595 = null;
                            Object statearr_20608 = object18;
                            ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20608, 1L, const__19);
                        }
                        object2 = const__20;
                        break;
                    }
                    case 3: {
                        Object inst_20577 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 9L);
                        Object statearr_20609 = this.state_20595;
                        Object object19 = inst_20577;
                        inst_20577 = null;
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20609, 2L, object19);
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20609, 1L, const__22);
                        object2 = const__20;
                        break;
                    }
                    case 4: {
                        Object inst_20580;
                        Object inst_20570 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 6L);
                        Object inst_20571 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 7L);
                        Object inst_20575 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 8L);
                        Object inst_20577 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 9L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 10L);
                        Object log3 = inst_20570;
                        Object item = inst_20575;
                        Object object20 = log3;
                        log3 = null;
                        Object object21 = item;
                        item = null;
                        Object object22 = inst_20580 = ((IFn)const__25.getRawRoot()).invoke(object20, object21);
                        inst_20580 = null;
                        Object inst_20581 = object22;
                        inst_20570 = null;
                        inst_20571 = null;
                        inst_20575 = null;
                        inst_20577 = null;
                        Object header = inst_20581;
                        ILookupThunk iLookupThunk = __thunk__0__;
                        Object object23 = header;
                        header = null;
                        Object object24 = iLookupThunk.get(object23);
                        if (iLookupThunk == object24) {
                            __thunk__0__ = __site__0__.fault(object23);
                            object24 = __thunk__0__.get(object23);
                        }
                        Object inst_20582 = object24;
                        Object statearr_20610 = this.state_20595;
                        Object object25 = inst_20581;
                        inst_20581 = null;
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20610, 10L, object25);
                        Object object26 = statearr_20610;
                        statearr_20610 = null;
                        Object state_20595 = object26;
                        Object object27 = inst_20582;
                        inst_20582 = null;
                        if (object27 != null && object27 != Boolean.FALSE) {
                            Object object28 = state_20595;
                            state_20595 = null;
                            Object statearr_20611 = object28;
                            ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20611, 1L, const__5);
                        } else {
                            Object object29 = state_20595;
                            state_20595 = null;
                            Object statearr_20612 = object29;
                            ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20612, 1L, const__6);
                        }
                        object2 = const__20;
                        break;
                    }
                    case 5: {
                        Object inst_20593;
                        Object object30 = inst_20593 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 2L);
                        inst_20593 = null;
                        object2 = ((IFn)const__28.getRawRoot()).invoke(this.state_20595, object30);
                        break;
                    }
                    case 6: {
                        Object inst_20581 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 10L);
                        Object statearr_20613 = this.state_20595;
                        Object object31 = inst_20581;
                        inst_20581 = null;
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20613, 2L, object31);
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20613, 1L, const__16);
                        object2 = const__20;
                        break;
                    }
                    case 7: {
                        Object inst_20587;
                        Object inst_20585;
                        Object inst_20570 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 6L);
                        Object inst_20571 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 7L);
                        Object inst_20575 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 8L);
                        Object inst_20577 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 9L);
                        Object inst_20581 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 10L);
                        Object tombstone = inst_20571;
                        Object header = inst_20581;
                        IFn iFn = (IFn)const__31.getRawRoot();
                        IFn iFn2 = (IFn)const__31.getRawRoot();
                        Object object32 = header;
                        ILookupThunk iLookupThunk = __thunk__1__;
                        Object object33 = header;
                        header = null;
                        Object object34 = iLookupThunk.get(object33);
                        if (iLookupThunk == object34) {
                            __thunk__1__ = __site__1__.fault(object33);
                            object34 = __thunk__1__.get(object33);
                        }
                        Object object35 = tombstone;
                        tombstone = null;
                        Object object36 = inst_20585 = iFn.invoke(iFn2.invoke(object32, (Object)const__32, object34), (Object)const__26, object35);
                        inst_20585 = null;
                        Object inst_20586 = object36;
                        Object object37 = inst_20570;
                        inst_20570 = null;
                        Object log4 = object37;
                        inst_20571 = null;
                        inst_20575 = null;
                        inst_20577 = null;
                        inst_20581 = null;
                        Object object38 = inst_20586;
                        inst_20586 = null;
                        Object new_header = object38;
                        Object object39 = log4;
                        log4 = null;
                        Object object40 = new_header;
                        new_header = null;
                        Object object41 = inst_20587 = ((IFn)const__34.getRawRoot()).invoke(object39, object40);
                        inst_20587 = null;
                        object2 = ((IFn)const__13.getRawRoot()).invoke(this.state_20595, const__17, object41);
                        break;
                    }
                    case 8: {
                        Object inst_20591 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 2L);
                        Object statearr_20614 = this.state_20595;
                        Object object42 = inst_20591;
                        inst_20591 = null;
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20614, 2L, object42);
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20614, 1L, const__22);
                        object2 = const__20;
                        break;
                    }
                    case 9: {
                        Object inst_20589 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 2L);
                        Object statearr_20615 = this.state_20595;
                        Object object43 = inst_20589;
                        inst_20589 = null;
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20615, 2L, object43);
                        ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20615, 1L, const__16);
                        object2 = const__20;
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__37.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__20604));
                    }
                }
                result__9978__auto__20623 = object2;
            } while (Util.identical((Object)result__9978__auto__20623, (Object)const__20));
            Object object44 = result__9978__auto__20623;
            result__9978__auto__20623 = null;
            object = object44;
        }
        catch (Throwable ex__9979__auto__2) {
            Object statearr_20616 = this.state_20595;
            ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20616, 2L, (Object)ex__9979__auto__2);
            Object object45 = ((IFn)const__39.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 4L));
            if (object45 == null || object45 == Boolean.FALSE) {
                Object ex__9979__auto__2 = null;
                throw ex__9979__auto__2;
            }
            Object statearr_20617 = this.state_20595;
            ((IFn.OLOO)const__12.getRawRoot()).invokePrim(statearr_20617, 1L, ((IFn)const__40.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20595, 4L)));
            object = const__20;
        }
        finally {
            this.state_20595 = null;
            ((IFn.OLOO)const__12.getRawRoot()).invokePrim(this.state_20595, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return object;
    }
}

