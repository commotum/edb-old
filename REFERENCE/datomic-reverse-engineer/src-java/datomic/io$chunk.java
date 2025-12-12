/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class io$chunk
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"integer?"), Symbol.intern(null, (String)"chunk-size")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 10}));
    public static final Object const__5 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"pos?"), Symbol.intern(null, (String)"chunk-size")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 33}));
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__9 = RT.keyword(null, (String)"default");
    public static final Var const__10 = RT.var((String)"datomic.io", (String)"seek");
    public static final Var const__11 = RT.var((String)"datomic.io", (String)"limit");

    public static Object invokeStatic(Object buf, Object chunk_size) {
        Object object;
        block5: {
            Object object2 = ((IFn)const__0.getRawRoot()).invoke(chunk_size);
            if (object2 == null || object2 == Boolean.FALSE) {
                throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
            }
            if (!Numbers.isPos((Object)chunk_size)) {
                throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__5))));
            }
            Object object3 = buf;
            buf = null;
            Object buf2 = ((ByteBuffer)object3).duplicate();
            Object chunks = PersistentVector.EMPTY;
            while (true) {
                if ((long)((Buffer)buf2).remaining() == 0L) {
                    object = chunks;
                    chunks = null;
                    break block5;
                }
                if (Numbers.lt((long)((Buffer)buf2).remaining(), (Object)chunk_size)) {
                    PersistentVector persistentVector = chunks;
                    chunks = null;
                    ByteBuffer byteBuffer = buf2;
                    buf2 = null;
                    object = ((IFn)const__8.getRawRoot()).invoke((Object)persistentVector, (Object)byteBuffer);
                    break block5;
                }
                Keyword keyword = const__9;
                if (keyword == null || keyword == Boolean.FALSE) break;
                Object object4 = ((IFn.OLO)const__10.getRawRoot()).invokePrim(buf2, RT.longCast((Object)((Number)chunk_size)));
                PersistentVector persistentVector = chunks;
                chunks = null;
                ByteBuffer byteBuffer = buf2;
                ByteBuffer byteBuffer2 = buf2;
                buf2 = null;
                chunks = ((IFn)const__8.getRawRoot()).invoke((Object)persistentVector, ((IFn.OLO)const__11.getRawRoot()).invokePrim((Object)byteBuffer, RT.longCast((Object)Numbers.add((long)((Buffer)byteBuffer2).position(), (Object)chunk_size))));
                buf2 = object4;
            }
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$chunk.invokeStatic(object3, object4);
    }
}

