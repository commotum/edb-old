/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$start_dirs_pipeline$fn__15434;
import datomic.index$start_dirs_pipeline$fn__15437;

public final class index$start_dirs_pipeline
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"cstore");
    public static final Keyword const__4 = RT.keyword(null, (String)"olookup");
    public static final Keyword const__5 = RT.keyword(null, (String)"idx");
    public static final Keyword const__6 = RT.keyword(null, (String)"dirnode-size");
    public static final Keyword const__7 = RT.keyword(null, (String)"dirs-ahead");
    public static final Keyword const__8 = RT.keyword(null, (String)"serialize-par");
    public static final Var const__9 = RT.var((String)"datomic.index", (String)"capture-last-ex");
    public static final Var const__13 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__15 = RT.var((String)"datomic.index", (String)"sparse-e-xf");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"partition-all");
    public static final Var const__17 = RT.var((String)"datomic.index", (String)"conjable-on-channel");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"add-watch");
    public static final Keyword const__20 = RT.keyword(null, (String)"fail-fast");
    public static final Var const__21 = RT.var((String)"clojure.core.async", (String)"pipeline");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__23 = RT.var((String)"datomic.index", (String)"serialize-dir");

    public static Object invokeStatic(ISeq p__15429) {
        ISeq iSeq;
        ISeq iSeq2 = p__15429;
        p__15429 = null;
        ISeq map__15430 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__15430);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__15430;
            map__15430 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__15430;
            map__15430 = null;
        }
        ISeq map__154302 = iSeq;
        Object cstore = RT.get((Object)map__154302, (Object)const__3);
        Object olookup = RT.get((Object)map__154302, (Object)const__4);
        Object idx = RT.get((Object)map__154302, (Object)const__5);
        Object dirnode_size = RT.get((Object)map__154302, (Object)const__6);
        Object dirs_ahead = RT.get((Object)map__154302, (Object)const__7);
        ISeq iSeq4 = map__154302;
        map__154302 = null;
        Object serialize_par = RT.get((Object)iSeq4, (Object)const__8);
        Object vec__15431 = ((IFn)const__9.getRawRoot()).invoke();
        Object ex_handler = RT.nth((Object)vec__15431, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__15431;
        vec__15431 = null;
        Object last_ex = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object serialized_dirs_ch = ((IFn)const__13.getRawRoot()).invoke();
        Object object3 = dirs_ahead;
        dirs_ahead = null;
        Object object4 = idx;
        idx = null;
        Object object5 = dirnode_size;
        dirnode_size = null;
        Object dir_entries_ch = ((IFn)const__13.getRawRoot()).invoke(object3, ((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(olookup, object4), ((IFn.LO)const__16.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object5)))), ex_handler);
        Object es = ((IFn)const__17.getRawRoot()).invoke(dir_entries_ch);
        Object object6 = cstore;
        cstore = null;
        Object object7 = olookup;
        olookup = null;
        Object fut = ((IFn)const__18.getRawRoot()).invoke((Object)new index$start_dirs_pipeline$fn__15434(object6, last_ex, serialized_dirs_ch, object7));
        Object object8 = last_ex;
        last_ex = null;
        ((IFn)const__19.getRawRoot()).invoke(object8, (Object)const__20, (Object)new index$start_dirs_pipeline$fn__15437(dir_entries_ch, serialized_dirs_ch));
        Object object9 = serialize_par;
        serialize_par = null;
        Object object10 = serialized_dirs_ch;
        serialized_dirs_ch = null;
        Object object11 = ex_handler;
        ex_handler = null;
        ((IFn)const__21.getRawRoot()).invoke(object9, object10, ((IFn)const__22.getRawRoot()).invoke(const__23.getRawRoot()), dir_entries_ch, (Object)Boolean.TRUE, object11);
        Object object12 = es;
        es = null;
        Object object13 = dir_entries_ch;
        dir_entries_ch = null;
        Object object14 = fut;
        fut = null;
        return Tuple.create((Object)object12, (Object)object13, (Object)object14);
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return index$start_dirs_pipeline.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

