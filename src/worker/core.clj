
(ns worker.core)

(def workers (ref {}))

;; Public
;; ------

(defmulti make-id
  "Return a vector for an ID"
  class)

(defn fetch
  "Try and fetch a future for the worker if it exists"
  [id]
  (get-in @workers (make-id id)))

(defn store
  "Store the future for the specified worker"
  [id f]
  (dosync
    (alter workers assoc-in (make-id id) f)))

(defn clear
  "A worker has finished, clear it from the ref"
  [id]
  (store id nil))

(defn result
  "Produce a result from a future"
  [f info]
  (if-let [data @f]
    (with-meta data info)))

(defmacro worker
  "Use futures to handle waiting on tasks already being processed"
  [id & body]
  `(if-let [waiting# (fetch ~id)]
     (result waiting# {:via-worker false})
     (let [f# (future ~@body)]
       (store ~id f#)
       (try
         (result f# {:via-worker true})
         (finally
           (clear ~id))))))

;; Worker ID Impls
;; ---------------

(defmethod make-id
  java.lang.String
  [id] [id])

(defmethod make-id
  clojure.lang.PersistentVector
  [id] id)

