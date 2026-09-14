/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cli$print_help$positional_QMARK___20672;

public final class cli$print_help
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*out*");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*err*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"format");
    public static final Keyword const__17 = RT.keyword(null, (String)"long-name");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__28 = RT.var((String)"datomic.cli", (String)"unique-index");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"short-name"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"long-name"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"doc"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"default"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"short-name"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"long-name"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"doc"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"default"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"long-name"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"doc"));
    static ILookupThunk __thunk__9__ = __site__9__;
    static final KeywordLookupSite __site__10__ = new KeywordLookupSite(RT.keyword(null, (String)"default"));
    static ILookupThunk __thunk__10__ = __site__10__;
    static final KeywordLookupSite __site__11__ = new KeywordLookupSite(RT.keyword(null, (String)"long-name"));
    static ILookupThunk __thunk__11__ = __site__11__;
    static final KeywordLookupSite __site__12__ = new KeywordLookupSite(RT.keyword(null, (String)"doc"));
    static ILookupThunk __thunk__12__ = __site__12__;
    static final KeywordLookupSite __site__13__ = new KeywordLookupSite(RT.keyword(null, (String)"default"));
    static ILookupThunk __thunk__13__ = __site__13__;

    public static Object invokeStatic(Object cmd, Object spec, Object positions) {
        Object var24_21;
        ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__2, const__3.get()));
        try {
            Object v124;
            Object pset;
            Object object = pset = ((IFn)const__4.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, positions);
            pset = null;
            cli$print_help$positional_QMARK___20672 positional_QMARK_ = new cli$print_help$positional_QMARK___20672(object);
            Object named = ((IFn)const__5.getRawRoot()).invoke((Object)positional_QMARK_, spec);
            cli$print_help$positional_QMARK___20672 cli$print_help$positional_QMARK___20672 = positional_QMARK_;
            positional_QMARK_ = null;
            Object object2 = spec;
            spec = null;
            Object positional = ((IFn)const__6.getRawRoot()).invoke((Object)cli$print_help$positional_QMARK___20672, object2);
            Object object3 = cmd;
            cmd = null;
            Object object4 = ((IFn)const__10.getRawRoot()).invoke(named);
            ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), (Object)"Usage:", ((IFn)const__9.getRawRoot()).invoke(object3, (Object)(object4 != null && object4 != Boolean.FALSE ? " {options}" : null)), ((IFn)const__11.getRawRoot()).invoke(const__12.getRawRoot(), positions));
            Object object5 = ((IFn)const__10.getRawRoot()).invoke(named);
            if (object5 != null && object5 != Boolean.FALSE) {
                ((IFn)const__8.getRawRoot()).invoke((Object)"\nOptions: ");
                Object object6 = named;
                named = null;
                Object seq_20674 = ((IFn)const__10.getRawRoot()).invoke(object6);
                Object chunk_20675 = null;
                long count_20676 = 0L;
                long i_20677 = 0L;
                while (true) {
                    Object object7;
                    Object temp__5457__auto__20687;
                    Object object8;
                    Object temp__5457__auto__20686;
                    Object temp__5457__auto__20688;
                    if (i_20677 < count_20676) {
                        Object object9;
                        Object temp__5457__auto__20684;
                        Object object10;
                        Object temp__5457__auto__20683;
                        Object opt = ((Indexed)chunk_20675).nth(RT.intCast((long)i_20677));
                        IFn iFn = (IFn)const__8.getRawRoot();
                        IFn iFn2 = (IFn)const__9.getRawRoot();
                        IFn iFn3 = (IFn)const__15.getRawRoot();
                        IFn iFn4 = (IFn)const__9.getRawRoot();
                        ILookupThunk iLookupThunk = __thunk__0__;
                        Object object11 = opt;
                        Object object12 = iLookupThunk.get(object11);
                        if (iLookupThunk == object12) {
                            __thunk__0__ = __site__0__.fault(object11);
                            object12 = __thunk__0__.get(object11);
                        }
                        Object object13 = temp__5457__auto__20683 = object12;
                        if (object13 != null && object13 != Boolean.FALSE) {
                            Object sn;
                            Object object14 = temp__5457__auto__20683;
                            temp__5457__auto__20683 = null;
                            Object object15 = sn = object14;
                            sn = null;
                            object10 = ((IFn)const__9.getRawRoot()).invoke((Object)"-", ((IFn)const__12.getRawRoot()).invoke(object15), (Object)",\n ");
                        } else {
                            object10 = null;
                        }
                        IFn iFn5 = (IFn)const__12.getRawRoot();
                        ILookupThunk iLookupThunk2 = __thunk__1__;
                        Object object16 = opt;
                        Object object17 = iLookupThunk2.get(object16);
                        if (iLookupThunk2 == object17) {
                            __thunk__1__ = __site__1__.fault(object16);
                            object17 = __thunk__1__.get(object16);
                        }
                        Object object18 = iFn3.invoke((Object)"%-20s: ", iFn4.invoke(object10, (Object)"--", iFn5.invoke(object17)));
                        ILookupThunk iLookupThunk3 = __thunk__2__;
                        Object object19 = opt;
                        Object object20 = iLookupThunk3.get(object19);
                        if (iLookupThunk3 == object20) {
                            __thunk__2__ = __site__2__.fault(object19);
                            object20 = __thunk__2__.get(object19);
                        }
                        ILookupThunk iLookupThunk4 = __thunk__3__;
                        Object object21 = opt;
                        opt = null;
                        Object object22 = iLookupThunk4.get(object21);
                        if (iLookupThunk4 == object22) {
                            __thunk__3__ = __site__3__.fault(object21);
                            object22 = __thunk__3__.get(object21);
                        }
                        Object object23 = temp__5457__auto__20684 = object22;
                        if (object23 != null && object23 != Boolean.FALSE) {
                            Object df;
                            Object object24 = temp__5457__auto__20684;
                            temp__5457__auto__20684 = null;
                            Object object25 = df = object24;
                            df = null;
                            object9 = ((IFn)const__9.getRawRoot()).invoke((Object)" (default ", object25, (Object)")");
                        } else {
                            object9 = null;
                        }
                        iFn.invoke(iFn2.invoke(object18, object20, object9));
                        Object object26 = seq_20674;
                        seq_20674 = null;
                        Object object27 = chunk_20675;
                        chunk_20675 = null;
                        ++i_20677;
                        chunk_20675 = object27;
                        seq_20674 = object26;
                        continue;
                    }
                    Object object28 = seq_20674;
                    seq_20674 = null;
                    Object object29 = temp__5457__auto__20688 = ((IFn)const__10.getRawRoot()).invoke(object28);
                    if (object29 == null || object29 == Boolean.FALSE) break;
                    Object object30 = temp__5457__auto__20688;
                    temp__5457__auto__20688 = null;
                    Object seq_206742 = object30;
                    Object object31 = ((IFn)const__21.getRawRoot()).invoke(seq_206742);
                    if (object31 != null && object31 != Boolean.FALSE) {
                        Object c__5719__auto__20685 = ((IFn)const__22.getRawRoot()).invoke(seq_206742);
                        Object object32 = seq_206742;
                        seq_206742 = null;
                        Object object33 = c__5719__auto__20685;
                        Object object34 = c__5719__auto__20685;
                        c__5719__auto__20685 = null;
                        i_20677 = RT.intCast((long)0L);
                        count_20676 = RT.intCast((int)RT.count((Object)object34));
                        chunk_20675 = object33;
                        seq_20674 = ((IFn)const__23.getRawRoot()).invoke(object32);
                        continue;
                    }
                    Object opt = ((IFn)const__26.getRawRoot()).invoke(seq_206742);
                    IFn iFn = (IFn)const__8.getRawRoot();
                    IFn iFn6 = (IFn)const__9.getRawRoot();
                    IFn iFn7 = (IFn)const__15.getRawRoot();
                    IFn iFn8 = (IFn)const__9.getRawRoot();
                    ILookupThunk iLookupThunk = __thunk__4__;
                    Object object35 = opt;
                    Object object36 = iLookupThunk.get(object35);
                    if (iLookupThunk == object36) {
                        __thunk__4__ = __site__4__.fault(object35);
                        object36 = __thunk__4__.get(object35);
                    }
                    Object object37 = temp__5457__auto__20686 = object36;
                    if (object37 != null && object37 != Boolean.FALSE) {
                        Object sn;
                        Object object38 = temp__5457__auto__20686;
                        temp__5457__auto__20686 = null;
                        Object object39 = sn = object38;
                        sn = null;
                        object8 = ((IFn)const__9.getRawRoot()).invoke((Object)"-", ((IFn)const__12.getRawRoot()).invoke(object39), (Object)",\n ");
                    } else {
                        object8 = null;
                    }
                    IFn iFn9 = (IFn)const__12.getRawRoot();
                    ILookupThunk iLookupThunk5 = __thunk__5__;
                    Object object40 = opt;
                    Object object41 = iLookupThunk5.get(object40);
                    if (iLookupThunk5 == object41) {
                        __thunk__5__ = __site__5__.fault(object40);
                        object41 = __thunk__5__.get(object40);
                    }
                    Object object42 = iFn7.invoke((Object)"%-20s: ", iFn8.invoke(object8, (Object)"--", iFn9.invoke(object41)));
                    ILookupThunk iLookupThunk6 = __thunk__6__;
                    Object object43 = opt;
                    Object object44 = iLookupThunk6.get(object43);
                    if (iLookupThunk6 == object44) {
                        __thunk__6__ = __site__6__.fault(object43);
                        object44 = __thunk__6__.get(object43);
                    }
                    ILookupThunk iLookupThunk7 = __thunk__7__;
                    Object object45 = opt;
                    opt = null;
                    Object object46 = iLookupThunk7.get(object45);
                    if (iLookupThunk7 == object46) {
                        __thunk__7__ = __site__7__.fault(object45);
                        object46 = __thunk__7__.get(object45);
                    }
                    Object object47 = temp__5457__auto__20687 = object46;
                    if (object47 != null && object47 != Boolean.FALSE) {
                        Object df;
                        Object object48 = temp__5457__auto__20687;
                        temp__5457__auto__20687 = null;
                        Object object49 = df = object48;
                        df = null;
                        object7 = ((IFn)const__9.getRawRoot()).invoke((Object)" (default ", object49, (Object)")");
                    } else {
                        object7 = null;
                    }
                    iFn.invoke(iFn6.invoke(object42, object44, object7));
                    Object object50 = seq_206742;
                    seq_206742 = null;
                    i_20677 = 0L;
                    count_20676 = 0L;
                    chunk_20675 = null;
                    seq_20674 = ((IFn)const__27.getRawRoot()).invoke(object50);
                }
            }
            Object object51 = ((IFn)const__10.getRawRoot()).invoke(positional);
            if (object51 != null && object51 != Boolean.FALSE) {
                ((IFn)const__8.getRawRoot()).invoke((Object)"\nPositional arguments:");
                Object object52 = positional;
                positional = null;
                Object idx = ((IFn)const__28.getRawRoot()).invoke(object52, (Object)const__17);
                Object object53 = positions;
                positions = null;
                Object seq_20678 = ((IFn)const__10.getRawRoot()).invoke(object53);
                Object chunk_20679 = null;
                long count_20680 = 0L;
                long i_20681 = 0L;
                while (true) {
                    Object temp__5457__auto__20693;
                    Object pos;
                    Object temp__5457__auto__20694;
                    if (i_20681 < count_20680) {
                        Object temp__5457__auto__20690;
                        Object pos2;
                        Object object54 = pos2 = ((Indexed)chunk_20679).nth(RT.intCast((long)i_20681));
                        pos2 = null;
                        Object object55 = temp__5457__auto__20690 = RT.get((Object)idx, (Object)object54);
                        if (object55 != null && object55 != Boolean.FALSE) {
                            Object object56;
                            Object temp__5457__auto__20689;
                            Object object57 = temp__5457__auto__20690;
                            temp__5457__auto__20690 = null;
                            Object arg2 = object57;
                            IFn iFn = (IFn)const__8.getRawRoot();
                            IFn iFn10 = (IFn)const__9.getRawRoot();
                            IFn iFn11 = (IFn)const__15.getRawRoot();
                            IFn iFn12 = (IFn)const__12.getRawRoot();
                            ILookupThunk iLookupThunk = __thunk__8__;
                            Object object58 = arg2;
                            Object object59 = iLookupThunk.get(object58);
                            if (iLookupThunk == object59) {
                                __thunk__8__ = __site__8__.fault(object58);
                                object59 = __thunk__8__.get(object58);
                            }
                            Object object60 = iFn11.invoke((Object)"%-20s: ", iFn12.invoke(object59));
                            ILookupThunk iLookupThunk8 = __thunk__9__;
                            Object object61 = arg2;
                            Object object62 = iLookupThunk8.get(object61);
                            if (iLookupThunk8 == object62) {
                                __thunk__9__ = __site__9__.fault(object61);
                                object62 = __thunk__9__.get(object61);
                            }
                            ILookupThunk iLookupThunk9 = __thunk__10__;
                            Object object63 = arg2;
                            arg2 = null;
                            Object object64 = iLookupThunk9.get(object63);
                            if (iLookupThunk9 == object64) {
                                __thunk__10__ = __site__10__.fault(object63);
                                object64 = __thunk__10__.get(object63);
                            }
                            Object object65 = temp__5457__auto__20689 = object64;
                            if (object65 != null && object65 != Boolean.FALSE) {
                                Object df;
                                Object object66 = temp__5457__auto__20689;
                                temp__5457__auto__20689 = null;
                                Object object67 = df = object66;
                                df = null;
                                object56 = ((IFn)const__9.getRawRoot()).invoke((Object)" (default ", object67, (Object)")");
                            } else {
                                object56 = null;
                            }
                            iFn.invoke(iFn10.invoke(object60, object62, object56));
                        }
                        Object object68 = seq_20678;
                        seq_20678 = null;
                        Object object69 = chunk_20679;
                        chunk_20679 = null;
                        ++i_20681;
                        chunk_20679 = object69;
                        seq_20678 = object68;
                        continue;
                    }
                    Object object70 = seq_20678;
                    seq_20678 = null;
                    Object object71 = temp__5457__auto__20694 = ((IFn)const__10.getRawRoot()).invoke(object70);
                    if (object71 == null || object71 == Boolean.FALSE) break;
                    Object object72 = temp__5457__auto__20694;
                    temp__5457__auto__20694 = null;
                    Object seq_206782 = object72;
                    Object object73 = ((IFn)const__21.getRawRoot()).invoke(seq_206782);
                    if (object73 != null && object73 != Boolean.FALSE) {
                        Object c__5719__auto__20691 = ((IFn)const__22.getRawRoot()).invoke(seq_206782);
                        Object object74 = seq_206782;
                        seq_206782 = null;
                        Object object75 = c__5719__auto__20691;
                        Object object76 = c__5719__auto__20691;
                        c__5719__auto__20691 = null;
                        i_20681 = RT.intCast((long)0L);
                        count_20680 = RT.intCast((int)RT.count((Object)object76));
                        chunk_20679 = object75;
                        seq_20678 = ((IFn)const__23.getRawRoot()).invoke(object74);
                        continue;
                    }
                    Object object77 = pos = ((IFn)const__26.getRawRoot()).invoke(seq_206782);
                    pos = null;
                    Object object78 = temp__5457__auto__20693 = RT.get((Object)idx, (Object)object77);
                    if (object78 != null && object78 != Boolean.FALSE) {
                        Object object79;
                        Object temp__5457__auto__20692;
                        Object object80 = temp__5457__auto__20693;
                        temp__5457__auto__20693 = null;
                        Object arg3 = object80;
                        IFn iFn = (IFn)const__8.getRawRoot();
                        IFn iFn13 = (IFn)const__9.getRawRoot();
                        IFn iFn14 = (IFn)const__15.getRawRoot();
                        IFn iFn15 = (IFn)const__12.getRawRoot();
                        ILookupThunk iLookupThunk = __thunk__11__;
                        Object object81 = arg3;
                        Object object82 = iLookupThunk.get(object81);
                        if (iLookupThunk == object82) {
                            __thunk__11__ = __site__11__.fault(object81);
                            object82 = __thunk__11__.get(object81);
                        }
                        Object object83 = iFn14.invoke((Object)"%-20s: ", iFn15.invoke(object82));
                        ILookupThunk iLookupThunk10 = __thunk__12__;
                        Object object84 = arg3;
                        Object object85 = iLookupThunk10.get(object84);
                        if (iLookupThunk10 == object85) {
                            __thunk__12__ = __site__12__.fault(object84);
                            object85 = __thunk__12__.get(object84);
                        }
                        ILookupThunk iLookupThunk11 = __thunk__13__;
                        Object object86 = arg3;
                        arg3 = null;
                        Object object87 = iLookupThunk11.get(object86);
                        if (iLookupThunk11 == object87) {
                            __thunk__13__ = __site__13__.fault(object86);
                            object87 = __thunk__13__.get(object86);
                        }
                        Object object88 = temp__5457__auto__20692 = object87;
                        if (object88 != null && object88 != Boolean.FALSE) {
                            Object df;
                            Object object89 = temp__5457__auto__20692;
                            temp__5457__auto__20692 = null;
                            Object object90 = df = object89;
                            df = null;
                            object79 = ((IFn)const__9.getRawRoot()).invoke((Object)" (default ", object90, (Object)")");
                        } else {
                            object79 = null;
                        }
                        iFn.invoke(iFn13.invoke(object83, object85, object79));
                    }
                    Object object91 = seq_206782;
                    seq_206782 = null;
                    i_20681 = 0L;
                    count_20680 = 0L;
                    chunk_20679 = null;
                    seq_20678 = ((IFn)const__27.getRawRoot()).invoke(object91);
                }
                v124 = null;
            } else {
                v124 = null;
            }
            var24_21 = v124;
        }
        finally {
            ((IFn)const__30.getRawRoot()).invoke();
        }
        return var24_21;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cli$print_help.invokeStatic(object4, object5, object6);
    }
}

