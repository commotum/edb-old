/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.fressian.Writer
 *  org.fressian.impl.BytesOutputStream
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import org.fressian.Writer;
import org.fressian.impl.BytesOutputStream;

public final class log$fressianed_tx
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.transaction", (String)"writer");
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__4 = RT.keyword(null, (String)"data");
    public static final Var const__6 = RT.var((String)"datomic.common", (String)"require-keys");
    public static final AFn const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"id"), (Object)RT.keyword(null, (String)"t"), (Object)RT.keyword(null, (String)"data"));
    public static final Var const__10 = RT.var((String)"datomic.io", (String)"bytestream->buf");

    public static Object invokeStatic(Object tx) {
        BytesOutputStream baos = new BytesOutputStream();
        Object fressian_out = ((IFn)const__0.getRawRoot()).invoke((Object)baos, (Object)(Numbers.gt((long)RT.count((Object)((IFn)const__3.getRawRoot()).invoke(tx, (Object)const__4)), (long)1L) ? Boolean.TRUE : Boolean.FALSE));
        ((Writer)fressian_out).resetCaches();
        Object object = fressian_out;
        fressian_out = null;
        Object object2 = tx;
        tx = null;
        ((Writer)object).writeObject(((IFn)const__6.getRawRoot()).invoke(object2, (Object)const__9));
        BytesOutputStream bytesOutputStream = baos;
        baos = null;
        return ((IFn)const__10.getRawRoot()).invoke((Object)bytesOutputStream);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$fressianed_tx.invokeStatic(object2);
    }
}

