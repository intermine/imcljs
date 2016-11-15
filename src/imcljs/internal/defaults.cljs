(ns imcljs.internal.defaults
  (:require [cljs.core.async :refer [chan]]
            [imcljs.query :refer [->xml]]
            [imcljs.internal.utils :refer [scrub-url]]))

(def missing? (complement contains?))

(def default-enrichment {:maxp 0.05 :widget "pathway_enrichment" :correction "Holm-Bonferroni"})

(defn url [root path]
  (str (scrub-url root) path))

(defn wrap-auth [request-map token]
  (if token
    (assoc-in request-map [:headers "authorization"] (str "Token " token))
    request-map))

(defn wrap-request-defaults [m & [xform]]
  (assoc m :with-credentials? false
           :channel (chan 1 (map (if xform (comp xform :body) :body)))))

(defn wrap-post-defaults [request options & [model]]
  (assoc request :form-params (cond-> options
                                      (contains? options :query) (assoc :query (->xml model (:query options)))
                                      (missing? options :format) (assoc :format "json"))))

(defn wrap-get-defaults [request options]
  (assoc request :query-params (cond-> options
                                       (missing? options :format) (assoc :format "json"))))

