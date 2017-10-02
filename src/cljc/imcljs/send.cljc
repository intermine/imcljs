(ns imcljs.send
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [imcljs.internal.io :refer [restful]]
            [imcljs.fetch :as fetch]
            [imcljs.internal.utils :refer [copy-list-query]]
    #?(:cljs [cljs.core.async :refer [<! >! chan]]
       :clj
            [clojure.core.async :refer [<! >! go chan]])))

(defn query-to-list
  "Save the results of a query to a list"
  [service query name & [options]]
  (restful :post "/query/tolist" service (assoc options :query query :name name)))

(defn copy-list
  "Copy a list by name"
  [service old-name new-name & [options]]
  (let [response-chan (chan)]
    ; Fetch the details from the previous list
    (go (let [old-list-details (<! (fetch/one-list service old-name))]
          ; Create a query that copies the list and save it to the server
          (println "RTCssdfsdf" (-> old-list-details copy-list-query))

          #_(>! response-chan (<! (query-to-list service (copy-list-query old-list-details) new-name)))))
    ; Return the channel that we just populated with the result
    response-chan))

(defn delete-list
  "Save the results of a query to a list"
  [service name & [options]]
  (restful :delete "/lists" service (assoc options :name name)))
