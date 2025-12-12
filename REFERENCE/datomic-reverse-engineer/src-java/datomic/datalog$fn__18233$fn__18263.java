/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$fn__18233$fn__18263$fn__18264;
import datomic.datalog$fn__18233$fn__18263$fn__18267;
import datomic.datalog$fn__18233$fn__18263$fn__18270;
import datomic.datalog$fn__18233$fn__18263$fn__18272;
import datomic.db.Attribute;
import datomic.db.IDb;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18263
extends AFunction {
    Object db;
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Keyword const__5 = RT.keyword(null, (String)"else");
    public static final Var const__6 = RT.var((String)"datomic.iter", (String)"filter");

    public datalog$fn__18233$fn__18263(Object object) {
        this.db = object;
    }

    public Object invoke(Object d) {
        Object object;
        Object attr = ((IDbImpl)this_.db).elementAt(((IDatum)d).getA());
        if (attr instanceof Attribute) {
            datalog$fn__18233$fn__18263 this_;
            Object object2 = ((Attribute)attr).hasAVET();
            if (object2 != null && object2 != Boolean.FALSE) {
                datalog$fn__18233$fn__18263$fn__18264 datalog$fn__18233$fn__18263$fn__18264 = new datalog$fn__18233$fn__18263$fn__18264(d);
                Object object3 = d;
                d = null;
                this_ = null;
                object = ((IFn)const__2.getRawRoot()).invoke(this_.db, (Object)datalog$fn__18233$fn__18263$fn__18264, (Object)((IDb)this_.db).seekAVET((IDatum)object3));
            } else {
                Object object4 = attr;
                attr = null;
                if (Util.equiv((long)20L, (Object)((Attribute)object4).vtypeid)) {
                    datalog$fn__18233$fn__18263$fn__18267 datalog$fn__18233$fn__18263$fn__18267 = new datalog$fn__18233$fn__18263$fn__18267(d);
                    Object object5 = d;
                    d = null;
                    this_ = null;
                    object = ((IFn)const__2.getRawRoot()).invoke(this_.db, (Object)datalog$fn__18233$fn__18263$fn__18267, (Object)((IDb)this_.db).seekRAET((IDatum)object5));
                } else {
                    Keyword keyword = const__5;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        datalog$fn__18233$fn__18263$fn__18270 datalog$fn__18233$fn__18263$fn__18270 = new datalog$fn__18233$fn__18263$fn__18270(d);
                        datalog$fn__18233$fn__18263$fn__18272 datalog$fn__18233$fn__18263$fn__18272 = new datalog$fn__18233$fn__18263$fn__18272(d);
                        Object object6 = d;
                        d = null;
                        this_ = null;
                        object = ((IFn)const__6.getRawRoot()).invoke((Object)datalog$fn__18233$fn__18263$fn__18270, ((IFn)const__2.getRawRoot()).invoke(this_.db, (Object)datalog$fn__18233$fn__18263$fn__18272, (Object)((IDb)this_.db).seekAEVT((IDatum)object6)));
                    } else {
                        object = null;
                    }
                }
            }
        } else {
            object = null;
        }
        return object;
    }
}

