/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.impl.ByteBufferInputStream
 *  org.fressian.impl.BytesOutputStream
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import org.fressian.impl.ByteBufferInputStream;
import org.fressian.impl.BytesOutputStream;

public final class io$gunzip_buffer
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.java.io.stream", (String)"buffered-input-stream");
    public static final Var const__9 = RT.var((String)"datomic.io", (String)"bytestream->buf");

    public static Object invokeStatic(Object buff) {
        Object object;
        long bsize = 4096L;
        byte[] bytes = Numbers.byte_array((Object)Numbers.num((long)bsize));
        ByteBufferInputStream is = new ByteBufferInputStream((ByteBuffer)buff);
        try {
            Object object2;
            GZIPInputStream gz = new GZIPInputStream((InputStream)is, RT.intCast((long)bsize));
            try {
                Object object3;
                Object bs = ((IFn)const__2.getRawRoot()).invoke((Object)gz);
                try {
                    Object object4;
                    Object object5 = buff;
                    buff = null;
                    BytesOutputStream os = new BytesOutputStream(RT.intCast((long)Numbers.multiply((long)2L, (long)((Buffer)object5).remaining())));
                    try {
                        while (true) {
                            int n;
                            if ((long)(n = ((InputStream)bs).read(bytes, RT.intCast((long)0L), bytes.length)) == -1L) break;
                            ((ByteArrayOutputStream)os).write(bytes, RT.intCast((long)0L), n);
                        }
                        object4 = ((IFn)const__9.getRawRoot()).invoke((Object)os);
                    }
                    finally {
                        BytesOutputStream bytesOutputStream = os;
                        os = null;
                        ((ByteArrayOutputStream)bytesOutputStream).close();
                    }
                    object3 = object4;
                }
                finally {
                    Object object6 = bs;
                    bs = null;
                    ((InputStream)object6).close();
                }
                object2 = object3;
            }
            finally {
                GZIPInputStream gZIPInputStream = gz;
                gz = null;
                gZIPInputStream.close();
            }
            object = object2;
        }
        finally {
            ByteBufferInputStream byteBufferInputStream = is;
            is = null;
            ((InputStream)byteBufferInputStream).close();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$gunzip_buffer.invokeStatic(object2);
    }
}

