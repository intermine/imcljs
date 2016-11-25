(ns imcljs.save
  (:require [imcljs.internal.io :refer [restful]]))


(defn im-list
  [service query & [options]]
  (restful :post "/query/tolist" service (merge {:query query :format "json"} options)))

(defn im-list-delete
  [service name & [options]]
  (restful :delete "/lists" service (merge {:name name :format "json"} options)))

(defn im-list-union
  [service name lists & [options]]
  (restful :post "/lists/union" service (merge {:lists lists :name name :format "json"} options)))