/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.io$unchunk$fn__9339;
import java.nio.ByteBuffer;

public final class io$unchunk
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object bbufs) {
        ByteBuffer result2 = ByteBuffer.allocate(RT.intCast((Object)((Number)((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke((Object)new io$unchunk$fn__9339(), bbufs)))));
        Object object = bbufs;
        bbufs = null;
        Object seq_9341 = ((IFn)const__3.getRawRoot()).invoke(object);
        Object chunk_9342 = null;
        long count_9343 = 0L;
        long i_9344 = 0L;
        while (true) {
            Object bbuf;
            Object temp__5457__auto__9347;
            if (i_9344 < count_9343) {
                Object bbuf2;
                Object object2 = bbuf2 = ((Indexed)chunk_9342).nth(RT.intCast((long)i_9344));
                bbuf2 = null;
                result2.put(((ByteBuffer)object2).duplicate());
                Object object3 = seq_9341;
                seq_9341 = null;
                Object object4 = chunk_9342;
                chunk_9342 = null;
                ++i_9344;
                chunk_9342 = object4;
                seq_9341 = object3;
                continue;
            }
            Object object5 = seq_9341;
            seq_9341 = null;
            Object object6 = temp__5457__auto__9347 = ((IFn)const__3.getRawRoot()).invoke(object5);
            if (object6 == null || object6 == Boolean.FALSE) break;
            Object object7 = temp__5457__auto__9347;
            temp__5457__auto__9347 = null;
            Object seq_93412 = object7;
            Object object8 = ((IFn)const__7.getRawRoot()).invoke(seq_93412);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object c__5719__auto__9346 = ((IFn)const__8.getRawRoot()).invoke(seq_93412);
                Object object9 = seq_93412;
                seq_93412 = null;
                Object object10 = c__5719__auto__9346;
                Object object11 = c__5719__auto__9346;
                c__5719__auto__9346 = null;
                i_9344 = RT.intCast((long)0L);
                count_9343 = RT.intCast((int)RT.count((Object)object11));
                chunk_9342 = object10;
                seq_9341 = ((IFn)const__9.getRawRoot()).invoke(object9);
                continue;
            }
            Object object12 = bbuf = ((IFn)const__12.getRawRoot()).invoke(seq_93412);
            bbuf = null;
            result2.put(((ByteBuffer)object12).duplicate());
            Object object13 = seq_93412;
            seq_93412 = null;
            i_9344 = 0L;
            count_9343 = 0L;
            chunk_9342 = null;
            seq_9341 = ((IFn)const__13.getRawRoot()).invoke(object13);
        }
        ByteBuffer byteBuffer = result2;
        result2 = null;
        return byteBuffer.flip();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$unchunk.invokeStatic(object2);
    }
}

