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
import java.io.File;

public final class clusterfs$create_files$fn__14264$fn__14266
extends AFunction {
    Object cs;
    Object chunk_size;
    public static final Var const__3 = RT.var((String)"clojure.java.io", (String)"file");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__5 = RT.keyword(null, (String)"base");
    public static final Var const__6 = RT.var((String)"datomic.clusterfs", (String)"create-file");
    public static final Keyword const__7 = RT.keyword(null, (String)"chunk-size");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__9 = RT.keyword(null, (String)"length");

    public clusterfs$create_files$fn__14264$fn__14266(Object object, Object object2) {
        this.cs = object;
        this.chunk_size = object2;
    }

    public Object invoke(Object m, Object p__14265) {
        Object or__5238__auto__14271;
        Object cname;
        Object object = p__14265;
        p__14265 = null;
        Object vec__14267 = object;
        Object lname = RT.nth((Object)vec__14267, (int)RT.intCast((long)0L), null);
        Object object2 = vec__14267;
        vec__14267 = null;
        Object object3 = cname = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        cname = null;
        Object cname2 = ((IFn)const__3.getRawRoot()).invoke(object3);
        Object object4 = m;
        m = null;
        Object object5 = cname2;
        cname2 = null;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__5;
        Object object6 = or__5238__auto__14271 = ((IFn)const__6.getRawRoot()).invoke(this_.cs, lname, (Object)const__7, this_.chunk_size);
        if (object6 == null || object6 == Boolean.FALSE) {
            throw (Throwable)new Error((String)((IFn)const__8.getRawRoot()).invoke((Object)"Unable to create clusterfs file ", lname));
        }
        Object object7 = or__5238__auto__14271;
        or__5238__auto__14271 = null;
        objectArray[1] = object7;
        objectArray[2] = const__9;
        Object object8 = lname;
        lname = null;
        objectArray[3] = Numbers.num((long)((File)((IFn)const__3.getRawRoot()).invoke(object8)).length());
        clusterfs$create_files$fn__14264$fn__14266 this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(object4, (Object)((File)object5).getPath(), (Object)RT.mapUniqueKeys((Object[])objectArray));
    }
}

