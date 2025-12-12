/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLL
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.IFn$OLO
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.Db;
import datomic.db.IProcess;
import datomic.db.IProcessExpander;
import datomic.db.LocalizeTempid;
import datomic.db.PartitionRequests;
import datomic.db.ProcessExpander$fn__14001;
import datomic.db.ProcessExpander$fn__14025;
import datomic.db.ProcessExpander$fn__14027;
import datomic.db.ProcessExpander$fn__14029;
import datomic.db.ProcessExpander$replace_tempid__14006;
import datomic.db.ProcessExpander$replace_tempids__14011;
import datomic.db.ProcessInpoint;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public final class ProcessExpander
implements IProcess,
IProcessExpander,
IType {
    public final Object db;
    public final Object part_reqs;
    public final Object arraylist;
    public final Object attr_hook_attrs;
    public final Object prefetch_dispatcher;
    public final Object tx_stat_registers;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Object const__2;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Var const__14;
    public static final Keyword const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__19;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__28;
    public static final Var const__29;
    public static final Var const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final Var const__34;
    public static final Var const__35;
    public static final Var const__36;
    public static final Var const__37;
    public static final Var const__38;
    public static final Var const__39;
    public static final Var const__40;
    public static final Var const__41;
    public static final Var const__42;
    public static final Var const__44;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;

    public ProcessExpander(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.db = object;
        this.part_reqs = object2;
        this.arraylist = object3;
        this.attr_hook_attrs = object4;
        this.prefetch_dispatcher = object5;
        this.tx_stat_registers = object6;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Db")})), (Object)((IObj)Symbol.intern(null, (String)"part-reqs")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"PartitionRequests")})), (Object)((IObj)Symbol.intern(null, (String)"arraylist")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ArrayList")})), (Object)Symbol.intern(null, (String)"attr-hook-attrs"), (Object)Symbol.intern(null, (String)"prefetch-dispatcher"), (Object)Symbol.intern(null, (String)"tx-stat-registers"));
    }

    public Object getData(Object local_tempids) {
        Object object;
        Object datoms2;
        ProcessExpander$replace_tempid__14006 replace_tempid;
        long start__13414__auto__14032 = System.nanoTime();
        Object ret__13415__auto__14033 = ((IFn)const__29.getRawRoot()).invoke(this.db, this.arraylist, this.part_reqs);
        IFn.OLO oLO = (IFn.OLO)const__19.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__3__;
        Object object2 = this.tx_stat_registers;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__3__ = __site__3__.fault(object2);
            object3 = __thunk__3__.get(object2);
        }
        oLO.invokePrim(object3, System.nanoTime() - start__13414__auto__14032);
        Object object4 = ret__13415__auto__14033;
        ret__13415__auto__14033 = null;
        Object vec__14003 = object4;
        Object ids = RT.nth((Object)vec__14003, (int)RT.uncheckedIntCast((long)0L), null);
        Object object5 = vec__14003;
        vec__14003 = null;
        Object check_installs_QMARK_ = RT.nth((Object)object5, (int)RT.uncheckedIntCast((long)1L), null);
        long basis = ((Db)this.db).nextT();
        ProcessExpander$replace_tempid__14006 processExpander$replace_tempid__14006 = replace_tempid = new ProcessExpander$replace_tempid__14006(ids, local_tempids, basis);
        replace_tempid = null;
        ProcessExpander$replace_tempids__14011 replace_tempids = new ProcessExpander$replace_tempids__14011(this.db, ids, (Object)processExpander$replace_tempid__14006);
        Date now = new Date();
        ProcessExpander$replace_tempids__14011 processExpander$replace_tempids__14011 = replace_tempids;
        replace_tempids = null;
        Object datoms3 = ((IFn)const__31.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__32.getRawRoot()).invoke(((IFn)const__33.getRawRoot()).invoke((Object)processExpander$replace_tempids__14011), ((IFn)const__34.getRawRoot()).invoke((Object)new ProcessExpander$fn__14025(this.db)), ((IFn)const__35.getRawRoot()).invoke(((IFn)const__36.getRawRoot()).invoke(this.prefetch_dispatcher, this.tx_stat_registers, this.db))), this.arraylist);
        ((IFn)const__37.getRawRoot()).invoke(this.prefetch_dispatcher, this.tx_stat_registers, this.db, datoms3);
        Object cs = ((IFn)const__38.getRawRoot()).invoke(this.db, datoms3, this.tx_stat_registers);
        ((IFn)const__37.getRawRoot()).invoke(this.prefetch_dispatcher, this.tx_stat_registers, this.db, cs);
        Object object6 = datoms3;
        datoms3 = null;
        Object object7 = cs;
        cs = null;
        Object object8 = datoms2 = ((IFn)const__31.getRawRoot()).invoke(object6, object7);
        datoms2 = null;
        Object object9 = check_installs_QMARK_;
        check_installs_QMARK_ = null;
        Object object10 = local_tempids;
        local_tempids = null;
        Object datoms4 = ((IFn)const__39.getRawRoot()).invoke((Object)new ProcessExpander$fn__14027(this.db, object8, object9), (Object)new ProcessExpander$fn__14029(ids, object10));
        Object object11 = ((IFn)const__40.getRawRoot()).invoke(this.db, (Object)now, datoms4);
        if (object11 != null && object11 != Boolean.FALSE) {
            object = datoms4;
            datoms4 = null;
        } else {
            Date date = now;
            now = null;
            Object object12 = datoms4;
            datoms4 = null;
            object = ((IFn)const__41.getRawRoot()).invoke(((IFn.LLOLO)const__22.getRawRoot()).invokePrim(((IFn.LLL)const__42.getRawRoot()).invokePrim(3L, basis), 50L, ((IFn)const__44.getRawRoot()).invoke(this.db, (Object)date), basis), object12);
        }
        Object object13 = ids;
        ids = null;
        return Tuple.create((Object)object, (Object)object13);
    }

    /*
     * Unable to fully structure code
     */
    public IProcess inject(Object procargs, Map local_tempids) {
        block18: {
            block15: {
                block17: {
                    block16: {
                        procid = ((IFn)ProcessExpander.const__0.getRawRoot()).invoke(this.db, RT.nth((Object)procargs, (int)RT.uncheckedIntCast((long)0L)));
                        or__5238__auto__14034 = Util.equiv((long)1L, (Object)procid);
                        if (!(or__5238__auto__14034 != false ? or__5238__auto__14034 : Util.equiv((long)2L, (Object)procid))) break block15;
                        v0 = RT.nth((Object)procargs, (int)RT.uncheckedIntCast((long)1L));
                        if (Util.classOf((Object)v0) == ProcessExpander.__cached_class__0) ** GOTO lbl9
                        if (!(v0 instanceof LocalizeTempid)) {
                            v0 = v0;
                            ProcessExpander.__cached_class__0 = Util.classOf((Object)v0);
lbl9:
                            // 2 sources

                            v1 = ProcessExpander.const__6.getRawRoot().invoke(v0, this.db, procargs, (Object)local_tempids);
                        } else {
                            v1 = ((LocalizeTempid)v0).local_id(this.db, procargs, local_tempids);
                        }
                        e = v1;
                        a = RT.nth((Object)procargs, (int)RT.uncheckedIntCast((long)2L));
                        attr = ((IFn)ProcessExpander.const__7.getRawRoot()).invoke(this.db, a);
                        ((Attribute)attr).id();
                        if (!Util.equiv((Object)((Attribute)attr).vtypeid, (long)20L)) break block16;
                        v2 = RT.nth((Object)procargs, (int)RT.uncheckedIntCast((long)3L));
                        if (Util.classOf((Object)v2) == ProcessExpander.__cached_class__1) ** GOTO lbl23
                        if (!(v2 instanceof LocalizeTempid)) {
                            v2 = v2;
                            ProcessExpander.__cached_class__1 = Util.classOf((Object)v2);
lbl23:
                            // 2 sources

                            v3 = procargs;
                            procargs = null;
                            v4 = local_tempids;
                            local_tempids = null;
                            v5 = ProcessExpander.const__6.getRawRoot().invoke(v2, this.db, v3, (Object)v4);
                        } else {
                            v6 = procargs;
                            procargs = null;
                            v7 = local_tempids;
                            local_tempids = null;
                            v5 = ((LocalizeTempid)v2).local_id(this.db, v6, v7);
                        }
                        break block17;
                    }
                    v8 = ProcessExpander.__thunk__0__;
                    v9 = attr;
                    v10 = v8.get(v9);
                    if (v8 == v10) {
                        ProcessExpander.__thunk__0__ = ProcessExpander.__site__0__.fault(v9);
                        v10 = ProcessExpander.__thunk__0__.get(v9);
                    }
                    if (v10 != null && v10 != Boolean.FALSE) {
                        v11 = RT.nth((Object)procargs, (int)RT.uncheckedIntCast((long)3L));
                        v12 = procargs;
                        procargs = null;
                        v13 = local_tempids;
                        local_tempids = null;
                        v5 = ((IFn)ProcessExpander.const__11.getRawRoot()).invoke(attr, v11, this.db, v12, (Object)v13);
                    } else {
                        v14 = ProcessExpander.const__12;
                        if (v14 != null && v14 != Boolean.FALSE) {
                            v15 = procargs;
                            procargs = null;
                            v5 = RT.nth((Object)v15, (int)RT.uncheckedIntCast((long)3L));
                        } else {
                            v5 = v = null;
                        }
                    }
                }
                if (Util.identical((Object)v, null)) {
                    ((IFn)ProcessExpander.const__14.getRawRoot()).invoke((Object)ProcessExpander.const__15, (Object)"nil value");
                }
                v16 = ((IFn)ProcessExpander.const__16.getRawRoot()).invoke(this.attr_hook_attrs, a);
                if (v16 != null && v16 != Boolean.FALSE) {
                    ((PartitionRequests)this.part_reqs).forcePart(e, ProcessExpander.const__2);
                }
                v17 = ((IFn)ProcessExpander.const__16.getRawRoot()).invoke(ProcessExpander.const__17.getRawRoot(), a);
                if (v17 != null && v17 != Boolean.FALSE) {
                    ((PartitionRequests)this.part_reqs).forcePart(v, ProcessExpander.const__2);
                }
                v18 = attr;
                attr = null;
                if (Util.equiv((Object)((Attribute)v18).unique, (long)38L)) {
                    v19 = (IFn.OLO)ProcessExpander.const__19.getRawRoot();
                    v20 = ProcessExpander.__thunk__1__;
                    v21 = this.tx_stat_registers;
                    v22 = v20.get(v21);
                    if (v20 == v22) {
                        ProcessExpander.__thunk__1__ = ProcessExpander.__site__1__.fault(v21);
                        v22 = ProcessExpander.__thunk__1__.get(v21);
                    }
                    v19.invokePrim(v22, 1L);
                    ((IFn)ProcessExpander.const__21.getRawRoot()).invoke(this.prefetch_dispatcher, this.tx_stat_registers, this.db, e, a, v);
                }
                v23 = procid;
                procid = null;
                v24 = e;
                e = null;
                v25 = a;
                a = null;
                v26 = v;
                v = null;
                v27 = ((ArrayList)this.arraylist).add(((IFn)(Util.equiv((Object)v23, (long)1L) != false ? ProcessExpander.const__22.getRawRoot() : ProcessExpander.const__23.getRawRoot())).invoke(v24, v25, v26, (Object)Numbers.num((long)((Db)this.db).nextT()))) != false ? Boolean.TRUE : Boolean.FALSE;
                break block18;
            }
            v28 = procid;
            procid = null;
            pfn = ((Db)this.db).getFn(v28);
            inp = new ProcessInpoint(this.db, this.part_reqs, this);
            start__13414__auto__14035 = System.nanoTime();
            v29 = pfn;
            pfn = null;
            v30 = procargs;
            procargs = null;
            ret__13415__auto__14036 = ((IFn)ProcessExpander.const__24.getRawRoot()).invoke((Object)v29, this.db, ((IFn)ProcessExpander.const__25.getRawRoot()).invoke(v30));
            v31 = (IFn.OLO)ProcessExpander.const__19.getRawRoot();
            v32 = ProcessExpander.__thunk__2__;
            v33 = this.tx_stat_registers;
            v34 = v32.get(v33);
            if (v32 == v34) {
                ProcessExpander.__thunk__2__ = ProcessExpander.__site__2__.fault(v33);
                v34 = ProcessExpander.__thunk__2__.get(v33);
            }
            v31.invokePrim(v34, System.nanoTime() - start__13414__auto__14035);
            v35 = ret__13415__auto__14036;
            ret__13415__auto__14036 = null;
            data = v35;
            v36 = local_tempids;
            local_tempids = null;
            v37 = inp;
            inp = null;
            v38 = data;
            data = null;
            ((IFn)ProcessExpander.const__28.getRawRoot()).invoke((Object)new ProcessExpander$fn__14001(v36), (Object)v37, v38);
        }
        return this;
    }

    static {
        const__0 = RT.var((String)"datomic.db", (String)"resolve-procid");
        const__2 = 0L;
        const__6 = RT.var((String)"datomic.db", (String)"local-id");
        const__7 = RT.var((String)"datomic.db", (String)"require-attr");
        const__11 = RT.var((String)"datomic.db", (String)"local-tuple");
        const__12 = RT.keyword(null, (String)"default");
        const__14 = RT.var((String)"datomic.error", (String)"arg");
        const__15 = RT.keyword((String)"db.error", (String)"nil-value");
        const__16 = RT.var((String)"clojure.core", (String)"contains?");
        const__17 = RT.var((String)"datomic.db", (String)"install-attrs");
        const__19 = RT.var((String)"datomic.db", (String)"long-add!");
        const__21 = RT.var((String)"datomic.db", (String)"prefetch-identity");
        const__22 = RT.var((String)"datomic.db", (String)"asserting-datum");
        const__23 = RT.var((String)"datomic.db", (String)"retracting-datum");
        const__24 = RT.var((String)"clojure.core", (String)"apply");
        const__25 = RT.var((String)"clojure.core", (String)"rest");
        const__28 = RT.var((String)"clojure.core", (String)"reduce");
        const__29 = RT.var((String)"datomic.db", (String)"get-ids");
        const__31 = RT.var((String)"clojure.core", (String)"into");
        const__32 = RT.var((String)"clojure.core", (String)"comp");
        const__33 = RT.var((String)"clojure.core", (String)"map");
        const__34 = RT.var((String)"clojure.core", (String)"remove");
        const__35 = RT.var((String)"datomic.db", (String)"for-side-effects");
        const__36 = RT.var((String)"datomic.db", (String)"composites-prefetcher");
        const__37 = RT.var((String)"datomic.db", (String)"prefetch-redundancy+uniqueness");
        const__38 = RT.var((String)"datomic.db", (String)"generate-composites");
        const__39 = RT.var((String)"datomic.db", (String)"add-tempids-to-errors");
        const__40 = RT.var((String)"datomic.db", (String)"has-tx-inst?");
        const__41 = RT.var((String)"clojure.core", (String)"cons");
        const__42 = RT.var((String)"datomic.db", (String)"make-eid");
        const__44 = RT.var((String)"datomic.db", (String)"next-valid-inst");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleRefOffsets"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"res-ct"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"tx-fn-ms"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"res-tx-ms"));
        __thunk__3__ = __site__3__;
    }
}

