(ns imcljs.io
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [cljs-http.client :refer [post get]]
            [imcljs.utils :refer [scrub-url]]
            [imcljs.query :refer [->xml]]))
(def missing? (complement contains?))

(defn path [service path]
  (str (scrub-url (:root service)) path))

(defn wrap-auth [service request-map]
  (if-let [token (get service :token)]
    (assoc-in request-map [:headers "authorization"] (str "Token " token))
    request-map))

(defn wrap-defaults [m]
  (assoc m :with-credentials? false))

(defn wrap-query-defaults [model m options]
  (cond-> (update m :query (partial ->xml model))
          (missing? m :format) (assoc :format "json")
          options (merge options)))



(defn table-rows
  "Returns the results of queries as table rows."
  [{:keys [service model]} query & [options]]
  (post (path service "/query/results/tablerows")
        (->> {}
             wrap-defaults
             (assoc :form-params (wrap-query-defaults model query options))
             (wrap-auth service))))