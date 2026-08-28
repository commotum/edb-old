(do
  (defonce KVStore {})
  (defprotocol KVStore
    (put [_ val-map])
    (get [_ key consistent?]))
  (extend java.lang.Throwable
    KVStore
    {:put (fn put-body ([_ val-map] [:put val-map]))
     :get (fn get-body ([_ key consistent?] [key consistent?]))}))
