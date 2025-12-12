/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.valcache$direct_put$fn__9820;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class valcache$direct_put
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"root");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"k");
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"v");
    public static final Var const__5 = RT.var((String)"datomic.valcache", (String)"full-path");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__7 = RT.classForName((String)"java.nio.file.OpenOption");
    public static final Object const__8 = RT.classForName((String)"java.nio.file.CopyOption");
    public static final Var const__9 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__10 = RT.keyword(null, (String)"event");
    public static final Keyword const__11 = RT.keyword((String)"valcache", (String)"put-exception");
    public static final Keyword const__12 = RT.keyword(null, (String)"tmp-path");
    public static final Keyword const__13 = RT.keyword(null, (String)"path");
    public static final Var const__14 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public static Object invokeStatic(Object root, Object k, Object v) {
        Boolean bl;
        Object temp__5457__auto__9823;
        Object object = root;
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__2))));
        }
        Object object2 = k;
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__3))));
        }
        Object object3 = v;
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__4))));
        }
        Object object4 = k;
        k = null;
        Object object5 = temp__5457__auto__9823 = ((IFn)const__5.getRawRoot()).invoke(root, object4);
        if (object5 != null && object5 != Boolean.FALSE) {
            Boolean bl2;
            Object object6 = temp__5457__auto__9823;
            temp__5457__auto__9823 = null;
            Object path2 = object6;
            Object object7 = root;
            root = null;
            Object tmp_path = ((IFn)const__5.getRawRoot()).invoke(object7, ((IFn)const__0.getRawRoot()).invoke((Object)UUID.randomUUID()));
            try {
                FileChannel fc;
                FileChannel fileChannel = fc = FileChannel.open((Path)tmp_path, (OpenOption[])((IFn)const__6.getRawRoot()).invoke(const__7, (Object)Tuple.create((Object)StandardOpenOption.CREATE, (Object)StandardOpenOption.WRITE, (Object)StandardOpenOption.TRUNCATE_EXISTING)));
                fc = null;
                Object object8 = v;
                v = null;
                ((IFn)new valcache$direct_put$fn__9820(fileChannel, object8)).invoke();
                Files.move((Path)tmp_path, (Path)path2, (CopyOption[])((IFn)const__6.getRawRoot()).invoke(const__8, (Object)Tuple.create((Object)StandardCopyOption.REPLACE_EXISTING)));
                bl2 = Boolean.TRUE;
            }
            catch (Throwable t2) {
                Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
                Object t2 = null;
                Throwable ex = t2;
                if (logger.isInfoEnabled()) {
                    Object[] objectArray = new Object[6];
                    objectArray[0] = const__10;
                    objectArray[1] = const__11;
                    objectArray[2] = const__12;
                    Object object9 = tmp_path;
                    tmp_path = null;
                    objectArray[3] = ((IFn)const__0.getRawRoot()).invoke(object9);
                    objectArray[4] = const__13;
                    Object object10 = path2;
                    path2 = null;
                    objectArray[5] = ((IFn)const__0.getRawRoot()).invoke(object10);
                    logger.info((String)((IFn)const__9.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)), ex);
                    Logger logger2 = logger;
                    logger = null;
                    Throwable throwable = ex;
                    ex = null;
                    ((IFn)const__14.getRawRoot()).invoke((Object)logger2, (Object)throwable);
                }
                bl2 = null;
            }
            bl = bl2;
        } else {
            bl = null;
        }
        return bl;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return valcache$direct_put.invokeStatic(object4, object5, object6);
    }
}

