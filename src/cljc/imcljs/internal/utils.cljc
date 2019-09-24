(ns imcljs.internal.utils
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clojure.string :refer [split]]
            #?(:cljs [cljs.core.async :as a :refer [<! >! chan]]
               :clj
               [clojure.core.async :as a :refer [<! >! go go-loop chan]])))

(def does-not-contain? (complement contains?))

(defn one-of? [haystack needle] (some? (some #{needle} haystack)))

(defn missing-http?- [val] (not (re-find #"^https?://" val)))

(defn missing-service?- [val] (not (re-find #"/service$" val)))

(defn append- [text val] (str val text))

(def alphabet (apply sorted-set (split "ABCDEFGHIJKLMNOPQRSTUVWXYZ" #"")))

(defn scrub-url
  "Ensures that a url starts with an http protocol and ends with /service"
  [url]
  (cond->> url
    (missing-http?- url) (str "http://")
    (missing-service?- url) (append- "/service")))

(defn copy-list-query
  [{old-list-name :name old-list-type :type :as old-list-details}]
  {:from old-list-type
   :select [(str old-list-type ".id")]
   :where [{:path old-list-type :op "IN" :value old-list-name}]})

(defn <<!
  "Given a collection of channels, returns a collection containing
  the first result of each channel (similiar to JS Promise.all)"
  [chans]
  (go-loop [coll '()
            chans chans]
    (if (seq chans)
      (recur (conj coll (<! (first chans)))
             (rest chans))
      coll)))

(defn assert-args
  [method & args]
  (let [[path service options & [xform]] (cond->> args
                                           (= method :raw) (drop 1))]
    (assert ((every-pred string? not-empty) path)
            "path should be a non-empty string.")
    (assert ((every-pred string? not-empty) (:root service))
            "service should always have a root URL.")
    (assert ((some-fn map? nil?) options)
            "options should be a map if non-nil.")
    (assert ((some-fn ifn? nil?) xform)
            "xform should be a callable function if non-nil.")))
