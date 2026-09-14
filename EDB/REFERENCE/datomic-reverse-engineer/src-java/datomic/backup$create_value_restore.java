/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.concurrent.Semaphore;

public final class backup$create_value_restore
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"from-storage");
    public static final Keyword const__4 = RT.keyword(null, (String)"to-cluster");
    public static final Keyword const__5 = RT.keyword(null, (String)"backup-version");
    public static final Keyword const__6 = RT.keyword(null, (String)"progress");
    public static final Keyword const__7 = RT.keyword(null, (String)"incremental?");
    public static final Keyword const__8 = RT.keyword(null, (String)"concurrency");
    public static final Keyword const__9 = RT.keyword(null, (String)"ids->nodes");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"from-storage");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"to-cluster");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"progress");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"concurrency");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"ids->nodes");
    public static final Var const__17 = RT.var((String)"datomic.backup", (String)"->ValueRestore");
    public static final Var const__18 = RT.var((String)"datomic.backup", (String)"substorage");
    public static final Var const__19 = RT.var((String)"datomic.backup", (String)"backup-k-factory");

    public static Object invokeStatic(ISeq p__20210) {
        ISeq iSeq;
        ISeq iSeq2 = p__20210;
        p__20210 = null;
        ISeq map__20211 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__20211);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__20211;
            map__20211 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__20211;
            map__20211 = null;
        }
        ISeq map__202112 = iSeq;
        Object from_storage = RT.get((Object)map__202112, (Object)const__3);
        Object to_cluster = RT.get((Object)map__202112, (Object)const__4);
        Object backup_version = RT.get((Object)map__202112, (Object)const__5);
        Object progress = RT.get((Object)map__202112, (Object)const__6);
        Object incremental_QMARK_ = RT.get((Object)map__202112, (Object)const__7);
        Object concurrency = RT.get((Object)map__202112, (Object)const__8);
        ISeq iSeq4 = map__202112;
        map__202112 = null;
        Object ids__GT_nodes = RT.get((Object)iSeq4, (Object)const__9);
        Object object2 = from_storage;
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke((Object)const__12))));
        }
        Object object3 = to_cluster;
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke((Object)const__13))));
        }
        Object object4 = progress;
        if (object4 == null || object4 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke((Object)const__14))));
        }
        Object object5 = concurrency;
        if (object5 == null || object5 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke((Object)const__15))));
        }
        Object object6 = ids__GT_nodes;
        if (object6 == null || object6 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke((Object)const__16))));
        }
        Object object7 = from_storage;
        from_storage = null;
        Object object8 = to_cluster;
        to_cluster = null;
        Object object9 = progress;
        progress = null;
        Object object10 = incremental_QMARK_;
        incremental_QMARK_ = null;
        Object object11 = backup_version;
        backup_version = null;
        Object object12 = ids__GT_nodes;
        ids__GT_nodes = null;
        Object object13 = concurrency;
        concurrency = null;
        return ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke(object7, (Object)"values"), object8, object9, object10, ((IFn.LO)const__19.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object11))), object12, (Object)new Semaphore(RT.intCast((Object)((Number)object13))));
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return backup$create_value_restore.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

