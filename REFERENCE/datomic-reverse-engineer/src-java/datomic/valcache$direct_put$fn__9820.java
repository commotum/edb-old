/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;

public final class valcache$direct_put$fn__9820
extends AFunction {
    Object fc;
    Object v;
    public static final Var const__1 = RT.var((String)"datomic.io", (String)"write-buffer");

    public valcache$direct_put$fn__9820(Object object, Object object2) {
        this.fc = object;
        this.v = object2;
    }

    public Object invoke() {
        Object var2_2;
        try {
            ByteBuffer bb;
            ByteBuffer byteBuffer = bb = ByteBuffer.allocate(RT.intCast((long)4L));
            bb = null;
            ((IFn)const__1.getRawRoot()).invoke((Object)byteBuffer.putInt(RT.intCast((long)2048L)).flip(), this.fc);
            ((IFn)const__1.getRawRoot()).invoke((Object)((ByteBuffer)this.v).duplicate(), this.fc);
            ((FileChannel)this.fc).force(Boolean.TRUE);
            var2_2 = null;
        }
        finally {
            ((AbstractInterruptibleChannel)this.fc).close();
        }
        return var2_2;
    }
}

