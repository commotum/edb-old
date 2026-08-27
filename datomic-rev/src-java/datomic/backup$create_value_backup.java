/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
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
import datomic.backup$create_value_backup$fn__20133;
import java.util.concurrent.Semaphore;

public final class backup$create_value_backup
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"from-cluster");
    public static final Keyword const__4 = RT.keyword(null, (String)"to-storage");
    public static final Keyword const__5 = RT.keyword(null, (String)"progress");
    public static final Keyword const__6 = RT.keyword(null, (String)"incremental?");
    public static final Keyword const__7 = RT.keyword(null, (String)"ids->nodes");
    public static final Keyword const__8 = RT.keyword(null, (String)"concurrency");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"from-cluster");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"to-storage");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"progress");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"ids->nodes");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"concurrency");
    public static final Var const__16 = RT.var((String)"datomic.backup", (String)"->ValueBackup");
    public static final Var const__17 = RT.var((String)"datomic.backup", (String)"substorage");
    public static final Var const__18 = RT.var((String)"datomic.config", (String)"property");

    public static Object invokeStatic(ISeq p__20131) {
        backup$create_value_backup$fn__20133 backup$create_value_backup$fn__20133;
        Object temp__5457__auto__20136;
        ISeq iSeq;
        ISeq iSeq2 = p__20131;
        p__20131 = null;
        ISeq map__20132 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__20132);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__20132;
            map__20132 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__20132;
            map__20132 = null;
        }
        ISeq map__201322 = iSeq;
        Object from_cluster = RT.get((Object)map__201322, (Object)const__3);
        Object to_storage = RT.get((Object)map__201322, (Object)const__4);
        Object progress = RT.get((Object)map__201322, (Object)const__5);
        Object incremental_QMARK_ = RT.get((Object)map__201322, (Object)const__6);
        Object ids__GT_nodes = RT.get((Object)map__201322, (Object)const__7);
        ISeq iSeq4 = map__201322;
        map__201322 = null;
        Object concurrency = RT.get((Object)iSeq4, (Object)const__8);
        Object object2 = from_cluster;
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__11))));
        }
        Object object3 = to_storage;
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__12))));
        }
        Object object4 = progress;
        if (object4 == null || object4 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__13))));
        }
        Object object5 = ids__GT_nodes;
        if (object5 == null || object5 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__14))));
        }
        Object object6 = concurrency;
        if (object6 == null || object6 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__15))));
        }
        IFn iFn = (IFn)const__16.getRawRoot();
        Object object7 = from_cluster;
        from_cluster = null;
        Object object8 = to_storage;
        to_storage = null;
        Object object9 = ((IFn)const__17.getRawRoot()).invoke(object8, (Object)"values");
        Object object10 = progress;
        progress = null;
        Object object11 = incremental_QMARK_;
        incremental_QMARK_ = null;
        Object object12 = ids__GT_nodes;
        ids__GT_nodes = null;
        Object object13 = temp__5457__auto__20136 = ((IFn)const__18.getRawRoot()).invoke((Object)"datomic.backupPaceMsec");
        if (object13 != null && object13 != Boolean.FALSE) {
            Object object14 = temp__5457__auto__20136;
            temp__5457__auto__20136 = null;
            Object pace = object14;
            pace = null;
            backup$create_value_backup$fn__20133 = new backup$create_value_backup$fn__20133(pace);
        } else {
            backup$create_value_backup$fn__20133 = null;
        }
        Object object15 = concurrency;
        concurrency = null;
        return iFn.invoke(object7, object9, object10, object11, object12, (Object)backup$create_value_backup$fn__20133, (Object)new Semaphore(RT.intCast((Object)((Number)object15))));
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return backup$create_value_backup.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

