/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.IProcess;
import java.util.List;
import java.util.Map;

public final class ProcessInpoint
implements IProcess,
IType {
    public final Object db;
    public final Object part_reqs;
    public final Object nextp;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"expand-map");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"force-map-keywords");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__16 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__21 = RT.var((String)"datomic.db", (String)"require-id");
    public static final Var const__22 = RT.var((String)"datomic.db", (String)"require-attr");
    public static final Var const__24 = RT.var((String)"datomic.db", (String)"inject-retracts!");
    public static final Var const__25 = RT.var((String)"datomic.db", (String)"validated-v-for-attr");
    public static final Keyword const__27 = RT.keyword(null, (String)"default");
    public static final Var const__28 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__29 = RT.keyword((String)"db.error", (String)"not-transaction-data");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"str");

    public ProcessInpoint(Object object, Object object2, Object object3) {
        this.db = object;
        this.part_reqs = object2;
        this.nextp = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Db")})), (Object)Symbol.intern(null, (String)"part-reqs"), (Object)((IObj)Symbol.intern(null, (String)"nextp")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IProcess")})));
    }

    public IProcess inject(Object procargs, Map local_tempids) {
        block8: {
            block9: {
                block7: {
                    if (!(procargs instanceof Map)) break block7;
                    Object object = procargs;
                    procargs = null;
                    Object seq_13854 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(this.db, ((IFn)const__4.getRawRoot()).invoke(this.db, object), this.part_reqs, (Object)local_tempids));
                    Object chunk_13855 = null;
                    long count_13856 = 0L;
                    long i_13857 = 0L;
                    while (true) {
                        Object x;
                        Object temp__5457__auto__13860;
                        if (i_13857 < count_13856) {
                            Object x2;
                            Object object2 = x2 = ((Indexed)chunk_13855).nth(RT.uncheckedIntCast((long)i_13857));
                            x2 = null;
                            ((IProcess)this).inject(object2, local_tempids);
                            Object object3 = seq_13854;
                            seq_13854 = null;
                            Object object4 = chunk_13855;
                            chunk_13855 = null;
                            ++i_13857;
                            chunk_13855 = object4;
                            seq_13854 = object3;
                            continue;
                        }
                        Object object5 = seq_13854;
                        seq_13854 = null;
                        Object object6 = temp__5457__auto__13860 = ((IFn)const__2.getRawRoot()).invoke(object5);
                        if (object6 == null || object6 == Boolean.FALSE) break;
                        Object object7 = temp__5457__auto__13860;
                        temp__5457__auto__13860 = null;
                        Object seq_138542 = object7;
                        Object object8 = ((IFn)const__8.getRawRoot()).invoke(seq_138542);
                        if (object8 != null && object8 != Boolean.FALSE) {
                            Object c__5719__auto__13859 = ((IFn)const__9.getRawRoot()).invoke(seq_138542);
                            Object object9 = seq_138542;
                            seq_138542 = null;
                            Object object10 = c__5719__auto__13859;
                            Object object11 = c__5719__auto__13859;
                            c__5719__auto__13859 = null;
                            i_13857 = (int)0L;
                            count_13856 = RT.count((Object)object11);
                            chunk_13855 = object10;
                            seq_13854 = ((IFn)const__10.getRawRoot()).invoke(object9);
                            continue;
                        }
                        Object object12 = x = ((IFn)const__13.getRawRoot()).invoke(seq_138542);
                        x = null;
                        ((IProcess)this).inject(object12, local_tempids);
                        Object object13 = seq_138542;
                        seq_138542 = null;
                        i_13857 = 0L;
                        count_13856 = 0L;
                        chunk_13855 = null;
                        seq_13854 = ((IFn)const__14.getRawRoot()).invoke(object13);
                    }
                    break block8;
                }
                if (!(procargs instanceof List)) break block9;
                Object procid = ((IFn)const__16.getRawRoot()).invoke(this.db, RT.nth((Object)procargs, (int)RT.uncheckedIntCast((long)0L)));
                boolean or__5238__auto__13861 = Util.equiv((long)1L, (Object)procid);
                if (or__5238__auto__13861 ? or__5238__auto__13861 : Util.equiv((long)2L, (Object)procid)) {
                    Object a;
                    Object eid = ((IFn)const__21.getRawRoot()).invoke(this.db, RT.nth((Object)procargs, (int)RT.uncheckedIntCast((long)1L)), procargs);
                    Object object = a = RT.nth((Object)procargs, (int)RT.uncheckedIntCast((long)2L));
                    a = null;
                    Object attr = ((IFn)const__22.getRawRoot()).invoke(this.db, object);
                    Object attrid = ((Attribute)attr).id();
                    boolean and__5236__auto__13862 = Util.equiv((long)2L, (Object)procid);
                    if (and__5236__auto__13862 ? Numbers.lt((long)RT.count((Object)procargs), (long)4L) : and__5236__auto__13862) {
                        Object object14 = eid;
                        eid = null;
                        Object object15 = attrid;
                        attrid = null;
                        Map map2 = local_tempids;
                        local_tempids = null;
                        ((IFn)const__24.getRawRoot()).invoke(this.nextp, this.db, object14, object15, (Object)map2);
                    } else {
                        Object object16 = attr;
                        attr = null;
                        Object object17 = RT.nth((Object)procargs, (int)RT.uncheckedIntCast((long)3L));
                        Object object18 = procargs;
                        procargs = null;
                        Object v = ((IFn)const__25.getRawRoot()).invoke(this.db, object16, object17, object18, procid);
                        Object object19 = procid;
                        procid = null;
                        Object object20 = eid;
                        eid = null;
                        Object object21 = attrid;
                        attrid = null;
                        Object object22 = v;
                        v = null;
                        Map map3 = local_tempids;
                        local_tempids = null;
                        ((IProcess)this.nextp).inject(Tuple.create((Object)object19, (Object)object20, (Object)object21, (Object)object22), map3);
                    }
                } else {
                    Object object = procargs;
                    procargs = null;
                    Map map4 = local_tempids;
                    local_tempids = null;
                    ((IProcess)this.nextp).inject(object, map4);
                }
                break block8;
            }
            Keyword keyword = const__27;
            if (keyword == null || keyword == Boolean.FALSE) break block8;
            Object object = procargs;
            procargs = null;
            ((IFn)const__28.getRawRoot()).invoke((Object)const__29, ((IFn)const__30.getRawRoot()).invoke((Object)"Transaction data element must be a List or Map, got ", object));
        }
        return this;
    }
}

