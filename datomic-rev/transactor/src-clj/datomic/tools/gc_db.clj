(do
  (clojure.core/in-ns 'datomic.tools.gc-db)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.instant :as 'instant]
        ['datomic.api :as 'd]
        ['datomic.coordination :as 'coord]
        ['datomic.garbage :as 'garbage]
        ['datomic.uri :as 'uri])))
  (when-not (.equals 'datomic.tools.gc-db 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.gc-db))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.instant :as 'instant]
          ['datomic.api :as 'd]
          ['datomic.coordination :as 'coord]
          ['datomic.garbage :as 'garbage]
          ['datomic.uri :as 'uri]))))
  (defn -main
    ([& args]
      (if (= (count args) 2)
        (let [vec__33233 args
              uri (nth vec__33233 (int 0) nil)
              tstamp_str (nth vec__33233 (int 1) nil)
              tstamp (instant/read-instant-date tstamp_str)]
          (try
            (let [cluster (coord/create-db-cluster
                            (coord/cluster-conf->resolved-conf (uri/parse uri)))
                  counter (atom 0)]
              (garbage/gc
                cluster
                tstamp
                (fn fn__33236 ([ct] (swap! counter + ct) (prn {:segments ct}))))
              (prn {:finished true, :segments (deref counter)}))
            (finally (d/shutdown true))))
        (do
          (println "Usage: datomic.tools.gc-db {uri} {older-than}")
          (println "older-than is an RFC3339-like timestamp.")
          (java.lang.System/exit (int -1))
          nil)))))