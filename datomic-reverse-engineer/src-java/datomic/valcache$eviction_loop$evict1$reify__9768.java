/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.PriorityQueue;

public final class valcache$eviction_loop$evict1$reify__9768
implements FileVisitor,
IObj {
    final IPersistentMap __meta;
    Object visited;
    Object size;
    Object opts;
    Object pq;
    Object file_window;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"inc");
    public static final Keyword const__6 = RT.keyword(null, (String)"atime");
    public static final Keyword const__7 = RT.keyword(null, (String)"length");
    public static final Keyword const__8 = RT.keyword(null, (String)"file");

    public valcache$eviction_loop$evict1$reify__9768(IPersistentMap iPersistentMap, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.__meta = iPersistentMap;
        this.visited = object;
        this.size = object2;
        this.opts = object3;
        this.pq = object4;
        this.file_window = object5;
    }

    public valcache$eviction_loop$evict1$reify__9768(Object object, Object object2, Object object3, Object object4, Object object5) {
        this(null, object, object2, object3, object4, object5);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new valcache$eviction_loop$evict1$reify__9768(iPersistentMap, this.visited, this.size, this.opts, this.pq, this.file_window);
    }

    public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
        block0: {
            Number length = Numbers.add((Object)Files.getAttribute((Path)file, "size", (LinkOption[])this.opts), (long)2048L);
            Object atime = Files.getAttribute((Path)file, "lastAccessTime", (LinkOption[])this.opts);
            ((IFn)const__0.getRawRoot()).invoke(this.size, const__1.getRawRoot(), (Object)length);
            ((IFn)const__0.getRawRoot()).invoke(this.visited, const__5.getRawRoot());
            Object[] objectArray = new Object[6];
            objectArray[0] = const__6;
            Object object = atime;
            atime = null;
            objectArray[1] = Numbers.num((long)((FileTime)object).toMillis());
            objectArray[2] = const__7;
            Number number = length;
            length = null;
            objectArray[3] = number;
            objectArray[4] = const__8;
            Object object2 = file;
            file = null;
            objectArray[5] = object2;
            Boolean bl = ((PriorityQueue)this.pq).add(RT.mapUniqueKeys((Object[])objectArray)) ? Boolean.TRUE : Boolean.FALSE;
            if (!Numbers.lte((Object)this.file_window, (long)RT.count((Object)this.pq))) break block0;
            ((PriorityQueue)this.pq).poll();
        }
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
        ((IFn)const__0.getRawRoot()).invoke(this.size, const__1.getRawRoot(), (Object)Numbers.num((long)Numbers.multiply((long)2L, (long)2048L)));
        return FileVisitResult.CONTINUE;
    }
}

