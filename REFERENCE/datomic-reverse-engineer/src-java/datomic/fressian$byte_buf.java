/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  org.fressian.impl.BytesOutputStream
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import org.fressian.impl.BytesOutputStream;

public final class fressian$byte_buf
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"datomic.fressian", (String)"fressian");
    public static final Var const__2 = RT.var((String)"datomic.io", (String)"bytestream->buf");

    public static Object invokeStatic(Object obj, ISeq options) {
        BytesOutputStream baos = new BytesOutputStream();
        Object object = obj;
        obj = null;
        ISeq iSeq = options;
        options = null;
        ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), (Object)baos, object, (Object)iSeq);
        BytesOutputStream bytesOutputStream = baos;
        baos = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)bytesOutputStream);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return fressian$byte_buf.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

