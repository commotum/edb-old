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
import datomic.log.LogDir;

public final class log$excise_root$patch_dir__16442$fn__16443
extends AFunction {
    Object replacements;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"conj");

    public log$excise_root$patch_dir__16442$fn__16443(Object object) {
        this.replacements = object;
    }

    public Object invoke(Object newdir, Object direntry) {
        Object object;
        Object temp__5455__auto__16445;
        Object eid = ((LogDir)direntry).uuid;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = newdir;
        newdir = null;
        Object object3 = eid;
        eid = null;
        Object object4 = temp__5455__auto__16445 = ((IFn)this_.replacements).invoke(object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5455__auto__16445;
            temp__5455__auto__16445 = null;
            Object neweid = object5;
            direntry = null;
            neweid = null;
            object = new LogDir(((LogDir)direntry).t, neweid);
        } else {
            object = direntry;
            direntry = null;
        }
        log$excise_root$patch_dir__16442$fn__16443 this_ = null;
        return iFn.invoke(object2, object);
    }
}

