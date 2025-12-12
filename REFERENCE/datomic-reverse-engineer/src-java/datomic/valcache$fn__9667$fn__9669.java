/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;

public final class valcache$fn__9667$fn__9669
extends AFunction {
    Object sc;
    int flags;
    Object vlen;
    Object fc;
    public static final Var const__1 = RT.var((String)"datomic.io", (String)"write-buffer");

    public valcache$fn__9667$fn__9669(Object object, int n, Object object2, Object object3) {
        this.sc = object;
        this.flags = n;
        this.vlen = object2;
        this.fc = object3;
    }

    public Object invoke() {
        Number number;
        try {
            ByteBuffer bb;
            ByteBuffer byteBuffer = bb = ByteBuffer.allocate(RT.intCast((long)4L));
            bb = null;
            ((IFn)const__1.getRawRoot()).invoke((Object)byteBuffer.putInt(this.flags).flip(), this.fc);
            ((FileChannel)this.fc).force(Boolean.TRUE);
            number = Numbers.num((long)((FileChannel)this.fc).transferFrom((ReadableByteChannel)this.sc, 4L, RT.longCast((Object)((Number)this.vlen))));
        }
        finally {
            ((AbstractInterruptibleChannel)this.fc).close();
        }
        return number;
    }
}

