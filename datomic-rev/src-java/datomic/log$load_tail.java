/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OL
 *  clojure.lang.IObj
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 *  org.fressian.Reader
 *  org.fressian.impl.ByteBufferInputStream
 *  org.fressian.impl.Codes
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.fressian.Reader;
import org.fressian.impl.ByteBufferInputStream;
import org.fressian.impl.Codes;

public final class log$load_tail
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__4 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"unchecked-byte"), Symbol.intern((String)"Codes", (String)"BEGIN_OPEN_LIST")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 22})), Symbol.intern(null, (String)"code")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 19}));
    public static final Var const__5 = RT.var((String)"datomic.fressian", (String)"create-reader");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"read-handlers");
    public static final Var const__7 = RT.var((String)"datomic.io", (String)"position");
    public static final Var const__9 = RT.var((String)"datomic.log", (String)"create-tail");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__12 = RT.var((String)"datomic.io", (String)"sub-buffer");

    public static Object invokeStatic(Object bbuf) {
        byte code = ((ByteBuffer)bbuf).get();
        if (RT.longCast((Object)RT.uncheckedByteCast((int)Codes.BEGIN_OPEN_LIST)) != RT.longCast((Object)code)) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__3.getRawRoot()).invoke(const__4))));
        }
        ByteBufferInputStream bbis = new ByteBufferInputStream((ByteBuffer)bbuf);
        Object fin = ((IFn)const__5.getRawRoot()).invoke((Object)bbis, const__6.getRawRoot(), (Object)Boolean.FALSE);
        Object txes = PersistentVector.EMPTY;
        Object bufs = PersistentVector.EMPTY;
        long pos = ((IFn.OL)const__7.getRawRoot()).invokePrim(bbuf);
        long remaining2 = bbis.available();
        while (true) {
            if (remaining2 == 0L) break;
            Object tx = ((Reader)fin).readObject();
            long size = Numbers.minus((long)remaining2, (long)bbis.available());
            PersistentVector persistentVector = txes;
            txes = null;
            Object object = tx;
            tx = null;
            PersistentVector persistentVector2 = bufs;
            bufs = null;
            Object object2 = ((IFn)const__11.getRawRoot()).invoke((Object)persistentVector2, ((IFn)const__12.getRawRoot()).invoke(bbuf, (Object)Numbers.num((long)pos), (Object)Numbers.num((long)size)));
            remaining2 = Numbers.minus((long)remaining2, (long)size);
            pos = Numbers.add((long)pos, (long)size);
            bufs = object2;
            txes = ((IFn)const__11.getRawRoot()).invoke((Object)persistentVector, object);
        }
        PersistentVector persistentVector = txes;
        txes = null;
        PersistentVector persistentVector3 = bufs;
        bufs = null;
        return ((IFn)const__9.getRawRoot()).invoke((Object)persistentVector, (Object)persistentVector3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$load_tail.invokeStatic(object2);
    }
}

