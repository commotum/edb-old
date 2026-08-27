/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.fressian.impl.ByteBufferInputStream
 */
package datomic.s3_kv;

import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.simple_kv.KV;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.fressian.impl.ByteBufferInputStream;

public final class S3Storage
implements KV,
IType {
    public final Object s3;
    public final Object bucket;
    public final Object base;
    public static final Var const__0 = RT.var((String)"datomic.s3", (String)"put-object");
    public static final Var const__1 = RT.var((String)"datomic.s3-kv", (String)"s3-storage-path");
    public static final Keyword const__2 = RT.keyword(null, (String)"contentLength");
    public static final Keyword const__3 = RT.keyword(null, (String)"ok");
    public static final Var const__4 = RT.var((String)"datomic.s3", (String)"get-direct-buffer");
    public static final Var const__5 = RT.var((String)"datomic.s3", (String)"delete-object");

    public S3Storage(Object object, Object object2, Object object3) {
        this.s3 = object;
        this.bucket = object2;
        this.base = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"s3"), (Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"base"));
    }

    public Object delete(Object k) {
        Object object = k;
        k = null;
        ((IFn)const__5.getRawRoot()).invoke(this.s3, this.bucket, ((IFn)const__1.getRawRoot()).invoke(this.base, object));
        return const__3;
    }

    public Object get(Object k) {
        Object object = k;
        k = null;
        S3Storage this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(this_.s3, this_.bucket, ((IFn)const__1.getRawRoot()).invoke(this_.base, object));
    }

    public Object put(Object id, Object v) {
        Keyword keyword;
        try {
            Object object = id;
            id = null;
            ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream((ByteBuffer)v);
            Object[] objectArray = new Object[2];
            objectArray[0] = const__2;
            Object object2 = v;
            v = null;
            objectArray[1] = ((Buffer)object2).remaining();
            ((IFn)const__0.getRawRoot()).invoke(this.s3, this.bucket, ((IFn)const__1.getRawRoot()).invoke(this.base, object), (Object)byteBufferInputStream, (Object)RT.mapUniqueKeys((Object[])objectArray));
            keyword = const__3;
        }
        catch (Exception ex) {
            keyword = null;
        }
        return keyword;
    }
}

