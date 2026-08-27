/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.impl.BytesOutputStream
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import org.fressian.impl.BytesOutputStream;

public final class io$gzip_buffer
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"alias-buf-bytes");
    public static final Object const__1 = 0L;
    public static final Var const__2 = RT.var((String)"datomic.io", (String)"bytestream->buf");

    public static Object invokeStatic(Object buff) {
        Object object;
        Object object2;
        byte[] bytes;
        Object object3 = buff;
        buff = null;
        ByteBuffer buff2 = ((ByteBuffer)object3).duplicate();
        int n = ((Buffer)buff2).remaining();
        Object object4 = bytes = buff2.hasArray() ? buff2.array() : (Object)((IFn)const__0.getRawRoot()).invoke((Object)buff2);
        if (buff2.hasArray()) {
            ByteBuffer byteBuffer = buff2;
            buff2 = null;
            object2 = byteBuffer.arrayOffset();
        } else {
            object2 = const__1;
        }
        Object offset = object2;
        BytesOutputStream os = new BytesOutputStream(n);
        try {
            Object object5;
            GZIPOutputStream gz = new GZIPOutputStream((OutputStream)os);
            try {
                Object object6;
                BufferedOutputStream bs = new BufferedOutputStream(gz);
                try {
                    byte[] byArray = bytes;
                    bytes = null;
                    Object object7 = offset;
                    offset = null;
                    bs.write(byArray, RT.intCast((Object)((Number)object7)), n);
                    bs.flush();
                    gz.finish();
                    object6 = ((IFn)const__2.getRawRoot()).invoke((Object)os);
                }
                finally {
                    BufferedOutputStream bufferedOutputStream = bs;
                    bs = null;
                    ((FilterOutputStream)bufferedOutputStream).close();
                }
                object5 = object6;
            }
            finally {
                GZIPOutputStream gZIPOutputStream = gz;
                gz = null;
                ((DeflaterOutputStream)gZIPOutputStream).close();
            }
            object = object5;
        }
        finally {
            BytesOutputStream bytesOutputStream = os;
            os = null;
            ((ByteArrayOutputStream)bytesOutputStream).close();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$gzip_buffer.invokeStatic(object2);
    }
}

