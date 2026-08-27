/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.external_sort.IO;
import java.io.File;
import java.util.UUID;

public final class external_sort$temp_file_io$reify__14398
implements IO,
IObj {
    final IPersistentMap __meta;
    Object dir;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.java.io", (String)"file");

    public external_sort$temp_file_io$reify__14398(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.dir = object;
    }

    public external_sort$temp_file_io$reify__14398(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new external_sort$temp_file_io$reify__14398(iPersistentMap, this.dir);
    }

    public Object delete_temp_file(Object f) {
        Object object = f;
        f = null;
        return ((File)object).delete() ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object temp_file_size(Object f) {
        Object object = f;
        f = null;
        return Numbers.num((long)((File)object).length());
    }

    public Object make_temp_file() {
        external_sort$temp_file_io$reify__14398 this_ = null;
        return File.createTempFile((String)((IFn)const__0.getRawRoot()).invoke((Object)UUID.randomUUID()), "", (File)((IFn)const__1.getRawRoot()).invoke(this_.dir));
    }
}

