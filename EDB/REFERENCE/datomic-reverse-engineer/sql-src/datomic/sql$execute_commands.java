/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.sql$execute_commands$fn__11526;
import datomic.sql$execute_commands$fn__11528;
import java.sql.Connection;
import java.sql.PreparedStatement;

public final class sql$execute_commands
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object conn, ISeq cmds) {
        ISeq iSeq = cmds;
        cmds = null;
        Object seq_11522 = ((IFn)const__0.getRawRoot()).invoke((Object)iSeq);
        Object chunk_11523 = null;
        long count_11524 = 0L;
        long i_11525 = 0L;
        while (true) {
            PreparedStatement stmt;
            Object cmd;
            Object temp__5457__auto__11532;
            if (i_11525 < count_11524) {
                PreparedStatement stmt2;
                Object cmd2;
                Object object = cmd2 = ((Indexed)chunk_11523).nth(RT.intCast((long)i_11525));
                cmd2 = null;
                PreparedStatement preparedStatement = stmt2 = ((Connection)conn).prepareStatement((String)object);
                stmt2 = null;
                ((IFn)new sql$execute_commands$fn__11526(preparedStatement)).invoke();
                Object object2 = seq_11522;
                seq_11522 = null;
                Object object3 = chunk_11523;
                chunk_11523 = null;
                ++i_11525;
                chunk_11523 = object3;
                seq_11522 = object2;
                continue;
            }
            Object object = seq_11522;
            seq_11522 = null;
            Object object4 = temp__5457__auto__11532 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object4 == null || object4 == Boolean.FALSE) break;
            Object object5 = temp__5457__auto__11532;
            temp__5457__auto__11532 = null;
            Object seq_115222 = object5;
            Object object6 = ((IFn)const__4.getRawRoot()).invoke(seq_115222);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object c__5719__auto__11531 = ((IFn)const__5.getRawRoot()).invoke(seq_115222);
                Object object7 = seq_115222;
                seq_115222 = null;
                Object object8 = c__5719__auto__11531;
                Object object9 = c__5719__auto__11531;
                c__5719__auto__11531 = null;
                i_11525 = RT.intCast((long)0L);
                count_11524 = RT.intCast((int)RT.count((Object)object9));
                chunk_11523 = object8;
                seq_11522 = ((IFn)const__6.getRawRoot()).invoke(object7);
                continue;
            }
            Object object10 = cmd = ((IFn)const__9.getRawRoot()).invoke(seq_115222);
            cmd = null;
            PreparedStatement preparedStatement = stmt = ((Connection)conn).prepareStatement((String)object10);
            stmt = null;
            ((IFn)new sql$execute_commands$fn__11528(preparedStatement)).invoke();
            Object object11 = seq_115222;
            seq_115222 = null;
            i_11525 = 0L;
            count_11524 = 0L;
            chunk_11523 = null;
            seq_11522 = ((IFn)const__10.getRawRoot()).invoke(object11);
        }
        return null;
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return sql$execute_commands.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}
