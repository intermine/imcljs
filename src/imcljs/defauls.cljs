(ns imcljs.defaults
  (:require [imcljs.query :refer [->xml]]
            [imcljs.utils :refer [scrub-url]]))

(def missing? (complement contains?))

(defn path [root path]
  (str (scrub-url root) path))

(defn wrap-auth [request-map token]
  (.log js/console "TOKEN" token)
  (if token
    (assoc-in request-map [:headers "authorization"] (str "Token " token))
    request-map))

(defn wrap-request-defaults [m]
  (assoc m :with-credentials? false))

(defn wrap-query-defaults [request model query options]
  (assoc request :form-params (cond-> options
                                      (missing? options :query) (assoc :query (->xml model query))
                                      (missing? options :format) (assoc :format "json"))))



