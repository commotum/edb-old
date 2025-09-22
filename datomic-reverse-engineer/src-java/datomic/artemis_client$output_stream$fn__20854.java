/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  org.apache.activemq.artemis.api.core.ActiveMQBuffer
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;

public final class artemis_client$output_stream$fn__20854
extends AFunction {
    Object buf;

    public artemis_client$output_stream$fn__20854(Object object) {
        this.buf = object;
    }

    public Object invoke(Object object, Object bs, Object off, Object len) {
        Object object2 = bs;
        bs = null;
        Object object3 = off;
        off = null;
        Object object4 = len;
        len = null;
        ((ActiveMQBuffer)this.buf).writeBytes((byte[])object2, RT.intCast((Object)object3), RT.intCast((Object)object4));
        return null;
    }

    public Object invoke(Object object, Object b) {
        Object object2 = b;
        b = null;
        ((ActiveMQBuffer)this.buf).writeByte(RT.uncheckedByteCast((Object)object2));
        return null;
    }
}

