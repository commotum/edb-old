/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.common$retry_fn$fn__9168;
import java.io.InterruptedIOException;

public final class common$retry_fn
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"pred");
    public static final Keyword const__4 = RT.keyword(null, (String)"backoff");
    public static final Keyword const__5 = RT.keyword(null, (String)"max-retries");
    public static final Keyword const__6 = RT.keyword(null, (String)"log-retry");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"number?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"constantly");
    public static final Var const__16 = RT.var((String)"datomic.common", (String)"return-or-throw");

    public static Object invokeStatic(Object f, ISeq p__9166) {
        Object object;
        block9: {
            Object result2;
            block8: {
                Object object2;
                ISeq iSeq;
                ISeq iSeq2 = p__9166;
                p__9166 = null;
                ISeq map__9167 = iSeq2;
                Object object3 = ((IFn)const__0.getRawRoot()).invoke((Object)map__9167);
                if (object3 != null && object3 != Boolean.FALSE) {
                    ISeq iSeq3 = map__9167;
                    map__9167 = null;
                    iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
                } else {
                    iSeq = map__9167;
                    map__9167 = null;
                }
                ISeq map__91672 = iSeq;
                Object pred2 = RT.get((Object)map__91672, (Object)const__3);
                Object backoff = RT.get((Object)map__91672, (Object)const__4);
                Object max_retries = RT.get((Object)map__91672, (Object)const__5);
                ISeq iSeq4 = map__91672;
                map__91672 = null;
                Object log_retry2 = RT.get((Object)iSeq4, (Object)const__6);
                Object object4 = ((IFn)const__7.getRawRoot()).invoke(backoff);
                if (object4 != null && object4 != Boolean.FALSE) {
                    Object object5 = backoff;
                    backoff = null;
                    object2 = ((IFn)const__8.getRawRoot()).invoke(object5);
                } else {
                    object2 = backoff;
                    backoff = null;
                }
                Object backoff2 = object2;
                long attempts = 1L;
                while (true) {
                    boolean or__5238__auto__9171;
                    if ((or__5238__auto__9171 = (result2 = ((IFn)new common$retry_fn$fn__9168(f)).invoke()) instanceof InterruptedException) ? or__5238__auto__9171 : result2 instanceof InterruptedIOException) {
                        throw (Throwable)result2;
                    }
                    Object object6 = ((IFn)pred2).invoke(result2);
                    if (object6 == null || object6 == Boolean.FALSE) break block8;
                    if (!Numbers.lt((long)attempts, (Object)max_retries)) break;
                    Object msec = ((IFn)backoff2).invoke((Object)Numbers.num((long)attempts));
                    Object object7 = log_retry2;
                    if (object7 != null && object7 != Boolean.FALSE) {
                        Object object8 = result2;
                        result2 = null;
                        ((IFn)log_retry2).invoke(object8, msec, (Object)Numbers.num((long)attempts), max_retries);
                    }
                    if (Numbers.isPos((Object)msec)) {
                        Object object9 = msec;
                        msec = null;
                        Thread.sleep(RT.uncheckedLongCast((Object)((Number)object9)));
                    }
                    ++attempts;
                }
                Object object10 = result2;
                result2 = null;
                object = ((IFn)const__16.getRawRoot()).invoke(object10);
                break block9;
            }
            Object object11 = result2;
            result2 = null;
            object = ((IFn)const__16.getRawRoot()).invoke(object11);
        }
        return object;
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return common$retry_fn.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

