(do
  (clojure.core/in-ns 'datomic.tools.rebuild-log-root)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.pprint :as 'pp]
        ['datomic.cluster :as 'cluster]
        ['datomic.common :as 'common]
        ['datomic.log :as 'log]
        ['datomic.log.specs :as 'log-specs]
        ['datomic.spec :refer (clojure.core/list 'conform!)]
        ['datomic.tools :as 'tools]
        ['datomic.tools.log-tools :as 'lt])))
  (when-not (.equals 'datomic.tools.rebuild-log-root 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.rebuild-log-root))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.pprint :as 'pp]
          ['datomic.cluster :as 'cluster]
          ['datomic.common :as 'common]
          ['datomic.log :as 'log]
          ['datomic.log.specs :as 'log-specs]
          ['datomic.spec :refer (clojure.core/list 'conform!)]
          ['datomic.tools :as 'tools]
          ['datomic.tools.log-tools :as 'lt]))))
  (set! *warn-on-reflection* true)
  (defn -main*
    ([& args]
      (let [G__30105 (count args)]
        (case
          G__30105
          1
          (let [cr (tools/connection-resources (first args))]
            (pp/pprint
              (lt/race-to-transform-root
                (:cluster cr)
                (fn fn__30106
                  ([root_key]
                    (let [map__30107 (lt/rebuild-root (:olookup cr) root_key)
                          map__30107 (if (seq? map__30107)
                                       (if (next map__30107)
                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                           (to-array map__30107))
                                         (if (seq map__30107) (first map__30107) {}))
                                       map__30107)
                          rebuilt (get map__30107 :rebuilt)
                          root (get map__30107 :root)]
                      (pp/pprint {:rebuilt rebuilt})
                      (if (seq rebuilt)
                        (let [id (common/rand-uuid)]
                          (datomic.spec/conform! :datomic.log/node root)
                          (lt/write* (:cluster cr) id (log/fressianed-dir root))
                          (cluster/uuid->val-key id))
                        root_key))))
                10)))
          2
          (let [cr (tools/connection-resources (second args))
                root_id (:d/r (first (log/read-tail-descriptor (:cluster cr))))]
            (println "Dry run. Changes needed: ")
            (pp/pprint (:rebuilt (lt/rebuild-root (:olookup cr) root_id))))))))
  (defn -main ([& args] (try (apply -main* args) (finally (shutdown-agents))))))