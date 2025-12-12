/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.integrity$crosscheck_log_cli$fn__22179;
import datomic.integrity$crosscheck_log_cli$fn__22182;

public final class integrity$crosscheck_log_cli
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"uri");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__5 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
    public static final Var const__6 = RT.var((String)"datomic.tools", (String)"connection-resources");
    public static final Var const__7 = RT.var((String)"datomic.api", (String)"connect");
    public static final AFn const__12 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"eavt"), (Object)RT.keyword(null, (String)"aevt"), (Object)RT.keyword(null, (String)"avet"), (Object)RT.keyword(null, (String)"vaet"));
    public static final Object const__13 = 0L;
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"flush");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__18 = RT.var((String)"datomic.log", (String)"find-log");
    public static final Var const__21 = RT.var((String)"datomic.integrity", (String)"crosscheck-log");
    public static final Var const__22 = RT.var((String)"datomic.api", (String)"db");
    public static final Keyword const__23 = RT.keyword(null, (String)"txes");
    public static final Keyword const__24 = RT.keyword(null, (String)"current-datoms");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"deref");
    public static final Keyword const__26 = RT.keyword(null, (String)"history-datoms");
    public static final Keyword const__27 = RT.keyword(null, (String)"nohistory-dropped");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__34 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__35 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cluster"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"olookup"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"cluster"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"olookup"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object p__22173) {
        Object map__22174;
        Object object;
        Object object2 = p__22173;
        p__22173 = null;
        Object map__221742 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__221742);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__221742;
            map__221742 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__221742;
            map__221742 = null;
        }
        Object object5 = map__22174 = object;
        map__22174 = null;
        Object uri2 = RT.get((Object)object5, (Object)const__3);
        ((IFn)const__4.getRawRoot()).invoke();
        Object object6 = uri2;
        uri2 = null;
        Object uri3 = ((IFn)const__5.getRawRoot()).invoke(object6);
        Object cr = ((IFn)const__6.getRawRoot()).invoke(uri3);
        Object object7 = uri3;
        uri3 = null;
        Object conn = ((IFn)const__7.getRawRoot()).invoke(object7);
        Object seq_22175 = ((IFn)const__1.getRawRoot()).invoke((Object)const__12);
        Object chunk_22176 = null;
        long count_22177 = 0L;
        long i_22178 = 0L;
        while (true) {
            Object log2;
            Object temp__5457__auto__22187;
            if (i_22178 < count_22177) {
                Object log3;
                Object index2 = ((Indexed)chunk_22176).nth(RT.intCast((long)i_22178));
                ((IFn)const__15.getRawRoot()).invoke((Object)"Checking ", index2, (Object)" against log");
                ((IFn)const__16.getRawRoot()).invoke();
                Object current = ((IFn)const__17.getRawRoot()).invoke(const__13);
                Object history2 = ((IFn)const__17.getRawRoot()).invoke(const__13);
                Object nohistory = ((IFn)const__17.getRawRoot()).invoke(const__13);
                IFn iFn = (IFn)const__18.getRawRoot();
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object8 = cr;
                Object object9 = iLookupThunk.get(object8);
                if (iLookupThunk == object9) {
                    __thunk__0__ = __site__0__.fault(object8);
                    object9 = __thunk__0__.get(object8);
                }
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object10 = cr;
                Object object11 = iLookupThunk2.get(object10);
                if (iLookupThunk2 == object11) {
                    __thunk__1__ = __site__1__.fault(object10);
                    object11 = __thunk__1__.get(object10);
                }
                Object object12 = log3 = iFn.invoke(object9, object11);
                log3 = null;
                Object object13 = index2;
                index2 = null;
                Object txes = ((IFn)const__21.getRawRoot()).invoke(object12, ((IFn)const__22.getRawRoot()).invoke(conn), object13, (Object)new integrity$crosscheck_log_cli$fn__22179(history2, nohistory, current));
                Object[] objectArray = new Object[8];
                objectArray[0] = const__23;
                Object object14 = txes;
                txes = null;
                objectArray[1] = object14;
                objectArray[2] = const__24;
                Object object15 = current;
                current = null;
                objectArray[3] = ((IFn)const__25.getRawRoot()).invoke(object15);
                objectArray[4] = const__26;
                Object object16 = history2;
                history2 = null;
                objectArray[5] = ((IFn)const__25.getRawRoot()).invoke(object16);
                objectArray[6] = const__27;
                Object object17 = nohistory;
                nohistory = null;
                objectArray[7] = ((IFn)const__25.getRawRoot()).invoke(object17);
                ((IFn)const__4.getRawRoot()).invoke((Object)"\n", (Object)RT.mapUniqueKeys((Object[])objectArray), (Object)"\n");
                Object object18 = seq_22175;
                seq_22175 = null;
                Object object19 = chunk_22176;
                chunk_22176 = null;
                ++i_22178;
                chunk_22176 = object19;
                seq_22175 = object18;
                continue;
            }
            Object object20 = seq_22175;
            seq_22175 = null;
            Object object21 = temp__5457__auto__22187 = ((IFn)const__1.getRawRoot()).invoke(object20);
            if (object21 == null || object21 == Boolean.FALSE) break;
            Object object22 = temp__5457__auto__22187;
            temp__5457__auto__22187 = null;
            Object seq_221752 = object22;
            Object object23 = ((IFn)const__29.getRawRoot()).invoke(seq_221752);
            if (object23 != null && object23 != Boolean.FALSE) {
                Object c__5719__auto__22186 = ((IFn)const__30.getRawRoot()).invoke(seq_221752);
                Object object24 = seq_221752;
                seq_221752 = null;
                Object object25 = c__5719__auto__22186;
                Object object26 = c__5719__auto__22186;
                c__5719__auto__22186 = null;
                i_22178 = RT.intCast((long)0L);
                count_22177 = RT.intCast((int)RT.count((Object)object26));
                chunk_22176 = object25;
                seq_22175 = ((IFn)const__31.getRawRoot()).invoke(object24);
                continue;
            }
            Object index3 = ((IFn)const__34.getRawRoot()).invoke(seq_221752);
            ((IFn)const__15.getRawRoot()).invoke((Object)"Checking ", index3, (Object)" against log");
            ((IFn)const__16.getRawRoot()).invoke();
            Object current = ((IFn)const__17.getRawRoot()).invoke(const__13);
            Object history3 = ((IFn)const__17.getRawRoot()).invoke(const__13);
            Object nohistory = ((IFn)const__17.getRawRoot()).invoke(const__13);
            IFn iFn = (IFn)const__18.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__2__;
            Object object27 = cr;
            Object object28 = iLookupThunk.get(object27);
            if (iLookupThunk == object28) {
                __thunk__2__ = __site__2__.fault(object27);
                object28 = __thunk__2__.get(object27);
            }
            ILookupThunk iLookupThunk3 = __thunk__3__;
            Object object29 = cr;
            Object object30 = iLookupThunk3.get(object29);
            if (iLookupThunk3 == object30) {
                __thunk__3__ = __site__3__.fault(object29);
                object30 = __thunk__3__.get(object29);
            }
            Object object31 = log2 = iFn.invoke(object28, object30);
            log2 = null;
            Object object32 = index3;
            index3 = null;
            Object txes = ((IFn)const__21.getRawRoot()).invoke(object31, ((IFn)const__22.getRawRoot()).invoke(conn), object32, (Object)new integrity$crosscheck_log_cli$fn__22182(current, nohistory, history3));
            Object[] objectArray = new Object[8];
            objectArray[0] = const__23;
            Object object33 = txes;
            txes = null;
            objectArray[1] = object33;
            objectArray[2] = const__24;
            Object object34 = current;
            current = null;
            objectArray[3] = ((IFn)const__25.getRawRoot()).invoke(object34);
            objectArray[4] = const__26;
            Object object35 = history3;
            history3 = null;
            objectArray[5] = ((IFn)const__25.getRawRoot()).invoke(object35);
            objectArray[6] = const__27;
            Object object36 = nohistory;
            nohistory = null;
            objectArray[7] = ((IFn)const__25.getRawRoot()).invoke(object36);
            ((IFn)const__4.getRawRoot()).invoke((Object)"\n", (Object)RT.mapUniqueKeys((Object[])objectArray), (Object)"\n");
            Object object37 = seq_221752;
            seq_221752 = null;
            i_22178 = 0L;
            count_22177 = 0L;
            chunk_22176 = null;
            seq_22175 = ((IFn)const__35.getRawRoot()).invoke(object37);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$crosscheck_log_cli.invokeStatic(object2);
    }
}

