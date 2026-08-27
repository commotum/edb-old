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
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
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
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class peer$ensure_schema_level
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.peer", (String)"db");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"tx-data-for-latest-schema-level");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__5 = RT.keyword(null, (String)"event");
    public static final Keyword const__6 = RT.keyword((String)"peer", (String)"ensure-schema-level");
    public static final Keyword const__7 = RT.keyword(null, (String)"db");
    public static final Keyword const__9 = RT.keyword(null, (String)"from");
    public static final Keyword const__10 = RT.keyword(null, (String)"to");
    public static final Var const__15 = RT.var((String)"datomic.peer", (String)"transact");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"schema-level"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object conn) {
        Object v25;
        Object temp__5457__auto__21689;
        Object db_before = ((IFn)const__0.getRawRoot()).invoke(conn);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = db_before;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object schema_level = object2;
        Object object3 = temp__5457__auto__21689 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(db_before));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5457__auto__21689;
            temp__5457__auto__21689 = null;
            Object txes = object4;
            Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                IFn iFn = (IFn)const__4.getRawRoot();
                Object[] objectArray = new Object[8];
                objectArray[0] = const__5;
                objectArray[1] = const__6;
                objectArray[2] = const__7;
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object5 = db_before;
                db_before = null;
                Object object6 = iLookupThunk2.get(object5);
                if (iLookupThunk2 == object6) {
                    __thunk__1__ = __site__1__.fault(object5);
                    object6 = __thunk__1__.get(object5);
                }
                objectArray[3] = object6;
                objectArray[4] = const__9;
                objectArray[5] = schema_level;
                objectArray[6] = const__10;
                Object object7 = schema_level;
                schema_level = null;
                objectArray[7] = Numbers.add((Object)object7, (long)RT.count((Object)txes));
                logger2.info((String)iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
            }
            Object object8 = txes;
            txes = null;
            Object seq_21682 = ((IFn)const__2.getRawRoot()).invoke(object8);
            Object chunk_21683 = null;
            long count_21684 = 0L;
            long i_21685 = 0L;
            while (true) {
                Object tx;
                Object temp__5457__auto__21688;
                if (i_21685 < count_21684) {
                    Object tx2;
                    Object object9 = tx2 = ((Indexed)chunk_21683).nth(RT.intCast((long)i_21685));
                    tx2 = null;
                    ((IFn)const__15.getRawRoot()).invoke(conn, object9);
                    Object object10 = seq_21682;
                    seq_21682 = null;
                    Object object11 = chunk_21683;
                    chunk_21683 = null;
                    ++i_21685;
                    chunk_21683 = object11;
                    seq_21682 = object10;
                    continue;
                }
                Object object12 = seq_21682;
                seq_21682 = null;
                Object object13 = temp__5457__auto__21688 = ((IFn)const__2.getRawRoot()).invoke(object12);
                if (object13 == null || object13 == Boolean.FALSE) break;
                Object object14 = temp__5457__auto__21688;
                temp__5457__auto__21688 = null;
                Object seq_216822 = object14;
                Object object15 = ((IFn)const__17.getRawRoot()).invoke(seq_216822);
                if (object15 != null && object15 != Boolean.FALSE) {
                    Object c__5719__auto__21687 = ((IFn)const__18.getRawRoot()).invoke(seq_216822);
                    Object object16 = seq_216822;
                    seq_216822 = null;
                    Object object17 = c__5719__auto__21687;
                    Object object18 = c__5719__auto__21687;
                    c__5719__auto__21687 = null;
                    i_21685 = RT.intCast((long)0L);
                    count_21684 = RT.intCast((int)RT.count((Object)object18));
                    chunk_21683 = object17;
                    seq_21682 = ((IFn)const__19.getRawRoot()).invoke(object16);
                    continue;
                }
                Object object19 = tx = ((IFn)const__21.getRawRoot()).invoke(seq_216822);
                tx = null;
                ((IFn)const__15.getRawRoot()).invoke(conn, object19);
                Object object20 = seq_216822;
                seq_216822 = null;
                i_21685 = 0L;
                count_21684 = 0L;
                chunk_21683 = null;
                seq_21682 = ((IFn)const__22.getRawRoot()).invoke(object20);
            }
            v25 = null;
        } else {
            v25 = null;
        }
        return v25;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$ensure_schema_level.invokeStatic(object2);
    }
}

