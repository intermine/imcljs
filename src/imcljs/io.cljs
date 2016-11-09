(ns imcljs.io
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [cljs-http.client :refer [post get]]
            [imcljs.utils :refer [scrub-url]]
            [imcljs.query :refer [->xml]]
            [imcljs.defaults :refer [url wrap-get-defaults wrap-request-defaults
                                     wrap-post-defaults wrap-auth]]))


(defn body-
  "Parses the body of the web service response.
  Accepts an optional transformation function for further parsing,
  such as :model to pull  out {:body {:model ..."
  [request-chan & [xform]]
  (go (cond-> (:body (<! request-chan)) xform xform)))

(defn post-wrapper-
  "Returns the results of queries as table rows."
  [path {:keys [root token model]} options]
  (post (url root path)
        (-> {} ; Blank request map
            wrap-request-defaults ; Add defaults such as with-credentials false?
            (wrap-post-defaults options model) ; Add form params
            (wrap-auth token))))


(defn request-wrapper-
  "Returns the results of queries as table rows."
  [path {:keys [root token model]} & [options]]
  (get (url root path)
       (-> {} ; Blank request map
           wrap-request-defaults ; Add defaults such as with-credentials false?
           (wrap-get-defaults options) ; Add query params
           (wrap-auth token))))

(defmulti restful identity)

(defmethod restful :post [method path service options & [xform]]
  (body- (post-wrapper- path service options) xform))

(defmethod restful :get [method path service options & [xform]]
  (body- (request-wrapper- path service options) xform))