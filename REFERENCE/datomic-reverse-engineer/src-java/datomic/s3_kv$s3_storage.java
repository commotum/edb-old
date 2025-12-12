/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.s3_kv.S3Storage;
import java.util.Arrays;

public final class s3_kv$s3_storage
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"s3");
    public static final Keyword const__4 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__5 = RT.keyword(null, (String)"base");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__8 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"s3"), Symbol.intern(null, (String)"bucket"), Symbol.intern(null, (String)"base")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));

    public static Object invokeStatic(ISeq p__23332) {
        Object object;
        Object and__5236__auto__23336;
        ISeq iSeq;
        ISeq iSeq2 = p__23332;
        p__23332 = null;
        ISeq map__23333 = iSeq2;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)map__23333);
        if (object2 != null && object2 != Boolean.FALSE) {
            ISeq iSeq3 = map__23333;
            map__23333 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__23333;
            map__23333 = null;
        }
        ISeq map__233332 = iSeq;
        Object s32 = RT.get((Object)map__233332, (Object)const__3);
        Object bucket = RT.get((Object)map__233332, (Object)const__4);
        ISeq iSeq4 = map__233332;
        map__233332 = null;
        Object base = RT.get((Object)iSeq4, (Object)const__5);
        Object object3 = and__5236__auto__23336 = s32;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object and__5236__auto__23335;
            Object object4 = and__5236__auto__23335 = bucket;
            if (object4 != null && object4 != Boolean.FALSE) {
                object = base;
            } else {
                object = and__5236__auto__23335;
                and__5236__auto__23335 = null;
            }
        } else {
            object = and__5236__auto__23336;
            and__5236__auto__23336 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__6.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__7.getRawRoot()).invoke(const__8))));
        }
        Object object5 = s32;
        s32 = null;
        Object object6 = bucket;
        bucket = null;
        Object object7 = base;
        base = null;
        return new S3Storage(object5, object6, object7);
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return s3_kv$s3_storage.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

