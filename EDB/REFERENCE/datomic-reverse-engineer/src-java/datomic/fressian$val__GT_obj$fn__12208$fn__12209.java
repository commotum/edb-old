/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Reader
 *  org.fressian.impl.ByteBufferInputStream
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import org.fressian.Reader;
import org.fressian.impl.ByteBufferInputStream;

public final class fressian$val__GT_obj$fn__12208$fn__12209
extends AFunction {
    Object read_lookup;
    Object val;
    public static final Var const__1 = RT.var((String)"datomic.java.io.stream", (String)"buffered-input-stream");
    public static final Var const__2 = RT.var((String)"datomic.fressian", (String)"create-reader");

    public fressian$val__GT_obj$fn__12208$fn__12209(Object object, Object object2) {
        this.read_lookup = object;
        this.val = object2;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            this.val = null;
            ByteBufferInputStream is = new ByteBufferInputStream((ByteBuffer)this.val);
            try {
                Object object3;
                GZIPInputStream gz = new GZIPInputStream((InputStream)is, RT.intCast((long)4096L));
                try {
                    Object object4;
                    Object bs = ((IFn)const__1.getRawRoot()).invoke((Object)gz);
                    try {
                        Object fr;
                        this.read_lookup = null;
                        Object object5 = fr = ((IFn)const__2.getRawRoot()).invoke(bs, this.read_lookup, (Object)Boolean.FALSE);
                        fr = null;
                        Object obj = ((Reader)object5).readObject();
                        ((InputStream)gz).readAllBytes();
                        Object object6 = obj;
                        obj = null;
                        object4 = object6;
                    }
                    finally {
                        Object object7 = bs;
                        bs = null;
                        ((InputStream)object7).close();
                    }
                    object3 = object4;
                }
                finally {
                    GZIPInputStream gZIPInputStream = gz;
                    gz = null;
                    gZIPInputStream.close();
                }
                object2 = object3;
            }
            finally {
                ByteBufferInputStream byteBufferInputStream = is;
                is = null;
                ((InputStream)byteBufferInputStream).close();
            }
            object = object2;
        }
        catch (Throwable e2) {
            Object e2 = null;
            object = e2;
        }
        return object;
    }
}

