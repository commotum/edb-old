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
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;
import datomic.integrity$crosscheck_log$fn__22147$fn__22152;

public final class integrity$crosscheck_log$fn__22147
extends AFunction {
    Object limit_tx;
    Object index_pred;
    Object index;
    Object db;
    Object tupler;
    Object hist;
    Object progress;
    Object basis_t;
    Object nohists;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__8 = RT.keyword(null, (String)"current");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__11 = RT.keyword(null, (String)"nohistory");
    public static final Keyword const__12 = RT.keyword(null, (String)"history");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Keyword const__16 = RT.keyword(null, (String)"datom");
    public static final Keyword const__17 = RT.keyword(null, (String)"dhist");
    public static final Keyword const__18 = RT.keyword(null, (String)"index");
    public static final Keyword const__19 = RT.keyword(null, (String)"basis-t");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public integrity$crosscheck_log$fn__22147(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.limit_tx = object;
        this.index_pred = object2;
        this.index = object3;
        this.db = object4;
        this.tupler = object5;
        this.hist = object6;
        this.progress = object7;
        this.basis_t = object8;
        this.nohists = object9;
    }

    public Object invoke(Object ctr, Object tx) {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        integrity$crosscheck_log$fn__22147$fn__22152 integrity$crosscheck_log$fn__22147$fn__22152 = new integrity$crosscheck_log$fn__22147$fn__22152(this_.limit_tx);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = tx;
        tx = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object seq_22148 = iFn.invoke(iFn2.invoke((Object)integrity$crosscheck_log$fn__22147$fn__22152, object2));
        Object chunk_22149 = null;
        long count_22150 = 0L;
        long i_22151 = 0L;
        while (true) {
            Object temp__5457__auto__22158;
            if (i_22151 < count_22150) {
                Object d = ((Indexed)chunk_22149).nth(RT.intCast((long)i_22151));
                boolean and__5236__auto__22155 = ((Datom)d).added();
                Object object3 = and__5236__auto__22155 ? ((IFn)this_.index_pred).invoke(d) : (and__5236__auto__22155 ? Boolean.TRUE : Boolean.FALSE);
                if (object3 != null && object3 != Boolean.FALSE) {
                    Object dnow;
                    Object object4 = dnow = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(this_.db, this_.index, ((IFn)this_.tupler).invoke(d)));
                    dnow = null;
                    if (Util.equiv((Object)d, (Object)object4)) {
                        Object object5 = this_.progress;
                        if (object5 != null && object5 != Boolean.FALSE) {
                            ((IFn)this_.progress).invoke((Object)const__8);
                        }
                    } else {
                        IFn iFn3 = (IFn)const__9.getRawRoot();
                        ILookupThunk iLookupThunk2 = __thunk__1__;
                        Object object6 = d;
                        Object object7 = iLookupThunk2.get(object6);
                        if (iLookupThunk2 == object7) {
                            __thunk__1__ = __site__1__.fault(object6);
                            object7 = __thunk__1__.get(object6);
                        }
                        Object object8 = iFn3.invoke(this_.nohists, object7);
                        if (object8 != null && object8 != Boolean.FALSE) {
                            Object object9 = this_.progress;
                            if (object9 != null && object9 != Boolean.FALSE) {
                                ((IFn)this_.progress).invoke((Object)const__11);
                            }
                        } else {
                            Object dhist = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(this_.hist, this_.index, ((IFn)this_.tupler).invoke(d)));
                            if (Util.equiv((Object)d, (Object)dhist)) {
                                Object object10 = this_.progress;
                                if (object10 != null && object10 != Boolean.FALSE) {
                                    ((IFn)this_.progress).invoke((Object)const__12);
                                }
                            } else {
                                Object object11 = ((IFn)const__14.getRawRoot()).invoke((Object)"Unable to seek to ", ((IFn)const__15.getRawRoot()).invoke(d), (Object)" in ", this_.index);
                                Object[] objectArray = new Object[8];
                                objectArray[0] = const__16;
                                Object object12 = d;
                                d = null;
                                objectArray[1] = object12;
                                objectArray[2] = const__17;
                                Object object13 = dhist;
                                dhist = null;
                                objectArray[3] = object13;
                                objectArray[4] = const__18;
                                objectArray[5] = this_.index;
                                objectArray[6] = const__19;
                                objectArray[7] = this_.basis_t;
                                throw (Throwable)((IFn)const__13.getRawRoot()).invoke(object11, (Object)RT.mapUniqueKeys((Object[])objectArray));
                            }
                        }
                    }
                }
                Object object14 = seq_22148;
                seq_22148 = null;
                Object object15 = chunk_22149;
                chunk_22149 = null;
                ++i_22151;
                chunk_22149 = object15;
                seq_22148 = object14;
                continue;
            }
            Object object16 = seq_22148;
            seq_22148 = null;
            Object object17 = temp__5457__auto__22158 = ((IFn)const__0.getRawRoot()).invoke(object16);
            if (object17 == null || object17 == Boolean.FALSE) break;
            Object object18 = temp__5457__auto__22158;
            temp__5457__auto__22158 = null;
            Object seq_221482 = object18;
            Object object19 = ((IFn)const__21.getRawRoot()).invoke(seq_221482);
            if (object19 != null && object19 != Boolean.FALSE) {
                Object c__5719__auto__22156 = ((IFn)const__22.getRawRoot()).invoke(seq_221482);
                Object object20 = seq_221482;
                seq_221482 = null;
                Object object21 = c__5719__auto__22156;
                Object object22 = c__5719__auto__22156;
                c__5719__auto__22156 = null;
                i_22151 = RT.intCast((long)0L);
                count_22150 = RT.intCast((int)RT.count((Object)object22));
                chunk_22149 = object21;
                seq_22148 = ((IFn)const__23.getRawRoot()).invoke(object20);
                continue;
            }
            Object d = ((IFn)const__5.getRawRoot()).invoke(seq_221482);
            boolean and__5236__auto__22157 = ((Datom)d).added();
            Object object23 = and__5236__auto__22157 ? ((IFn)this_.index_pred).invoke(d) : (and__5236__auto__22157 ? Boolean.TRUE : Boolean.FALSE);
            if (object23 != null && object23 != Boolean.FALSE) {
                Object dnow;
                Object object24 = dnow = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(this_.db, this_.index, ((IFn)this_.tupler).invoke(d)));
                dnow = null;
                if (Util.equiv((Object)d, (Object)object24)) {
                    Object object25 = this_.progress;
                    if (object25 != null && object25 != Boolean.FALSE) {
                        ((IFn)this_.progress).invoke((Object)const__8);
                    }
                } else {
                    IFn iFn4 = (IFn)const__9.getRawRoot();
                    ILookupThunk iLookupThunk3 = __thunk__2__;
                    Object object26 = d;
                    Object object27 = iLookupThunk3.get(object26);
                    if (iLookupThunk3 == object27) {
                        __thunk__2__ = __site__2__.fault(object26);
                        object27 = __thunk__2__.get(object26);
                    }
                    Object object28 = iFn4.invoke(this_.nohists, object27);
                    if (object28 != null && object28 != Boolean.FALSE) {
                        Object object29 = this_.progress;
                        if (object29 != null && object29 != Boolean.FALSE) {
                            ((IFn)this_.progress).invoke((Object)const__11);
                        }
                    } else {
                        Object dhist = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(this_.hist, this_.index, ((IFn)this_.tupler).invoke(d)));
                        if (Util.equiv((Object)d, (Object)dhist)) {
                            Object object30 = this_.progress;
                            if (object30 != null && object30 != Boolean.FALSE) {
                                ((IFn)this_.progress).invoke((Object)const__12);
                            }
                        } else {
                            Object object31 = ((IFn)const__14.getRawRoot()).invoke((Object)"Unable to seek to ", ((IFn)const__15.getRawRoot()).invoke(d), (Object)" in ", this_.index);
                            Object[] objectArray = new Object[8];
                            objectArray[0] = const__16;
                            Object object32 = d;
                            d = null;
                            objectArray[1] = object32;
                            objectArray[2] = const__17;
                            Object object33 = dhist;
                            dhist = null;
                            objectArray[3] = object33;
                            objectArray[4] = const__18;
                            objectArray[5] = this_.index;
                            objectArray[6] = const__19;
                            objectArray[7] = this_.basis_t;
                            throw (Throwable)((IFn)const__13.getRawRoot()).invoke(object31, (Object)RT.mapUniqueKeys((Object[])objectArray));
                        }
                    }
                }
            }
            Object object34 = seq_221482;
            seq_221482 = null;
            i_22151 = 0L;
            count_22150 = 0L;
            chunk_22149 = null;
            seq_22148 = ((IFn)const__26.getRawRoot()).invoke(object34);
        }
        Object object35 = ctr;
        ctr = null;
        integrity$crosscheck_log$fn__22147 this_ = null;
        return Numbers.inc((Object)object35);
    }
}

