(do
  (clojure.core/in-ns 'datomic.client-server.marshaling)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['cognitect.transit :as 't] ['datomic.db :as 'db])))
  (when-not (.equals 'datomic.client-server.marshaling 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.client-server.marshaling))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['cognitect.transit :as 't] ['datomic.db :as 'db]))))
  (def transit-write-opts
   {:handlers
    {datomic.Datom
     (t/write-handler
       (fn fn__24149 ([_] "datom"))
       (fn fn__24151
         ([d]
           (let [dt d]
             (t/tagged-value
               "array"
               [(.e ^datomic.Datom dt)
                (.a ^datomic.Datom dt)
                (.v ^datomic.Datom dt)
                (.tx ^datomic.Datom dt)
                (.added ^datomic.Datom dt)]))))),
     datomic.db.DbId
     (t/write-handler
       (fn fn__24153 ([_] "dbid"))
       (fn fn__24155
         ([d] (let [dbid d] [(.-part ^datomic.db.DbId dbid) (.-idx ^datomic.db.DbId dbid)]))))}})
  (def transit-read-opts
   {:handlers
    {"r" (t/read-handler (fn fn__24157 ([r] (java.net.URI. ^java.lang.String r)))),
     "dbid" (t/read-handler db/id-literal),
     "datom"
     (t/read-handler
       (fn fn__24160
         ([p__24159]
           (let [vec__24161 p__24159
                 e (nth vec__24161 (int 0) nil)
                 a (nth vec__24161 (int 1) nil)
                 v (nth vec__24161 (int 2) nil)
                 tx (nth vec__24161 (int 3) nil)
                 op (nth vec__24161 (int 4) nil)]
             ((if op db/asserting-datum db/retracting-datum)
               e
               a
               v
               (long (db/eid->eidx (long ^java.lang.Number tx))))))))}})
  (def instance
   {"application/transit+json" {:write-opts transit-write-opts, :read-opts transit-read-opts},
    "application/transit+msgpack" {:write-opts transit-write-opts, :read-opts transit-read-opts}}))