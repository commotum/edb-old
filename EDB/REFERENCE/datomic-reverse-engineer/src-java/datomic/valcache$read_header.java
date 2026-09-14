/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class valcache$read_header
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"read-into-buffer");
    public static final Object const__1 = 24L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__5 = RT.keyword(null, (String)"opcode");
    public static final Keyword const__6 = RT.keyword(null, (String)"key-length");
    public static final Keyword const__7 = RT.keyword(null, (String)"extras-length");
    public static final Keyword const__8 = RT.keyword(null, (String)"data-type");
    public static final Keyword const__9 = RT.keyword(null, (String)"vbucket-id");
    public static final Keyword const__10 = RT.keyword(null, (String)"total-body-length");
    public static final Keyword const__11 = RT.keyword(null, (String)"cas");

    public static Object invokeStatic(Object sc, Object bb) {
        Object object = sc;
        sc = null;
        ((IFn)const__0.getRawRoot()).invoke(bb, const__1, object);
        byte magic = ((ByteBuffer)bb).get();
        if (-128L != RT.longCast((Object)magic)) {
            throw (Throwable)new IOException((String)((IFn)const__4.getRawRoot()).invoke((Object)"Framing error: ", (Object)magic));
        }
        byte opcode = ((ByteBuffer)bb).get();
        short key_length = ((ByteBuffer)bb).getShort();
        byte extras_length = ((ByteBuffer)bb).get();
        byte data_type = ((ByteBuffer)bb).get();
        short vbucket_id = ((ByteBuffer)bb).getShort();
        int total_body_length = ((ByteBuffer)bb).getInt();
        int opaque = ((ByteBuffer)bb).getInt();
        Object object2 = bb;
        bb = null;
        long cas = ((ByteBuffer)object2).getLong();
        return RT.mapUniqueKeys((Object[])new Object[]{const__5, opcode, const__6, key_length, const__7, extras_length, const__8, data_type, const__9, vbucket_id, const__10, total_body_length, const__11, Numbers.num((long)cas)});
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$read_header.invokeStatic(object3, object4);
    }
}

