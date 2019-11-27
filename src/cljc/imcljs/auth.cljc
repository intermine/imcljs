(ns imcljs.auth
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [imcljs.internal.io :refer [restful]]
            #?(:cljs [cljs.core.async :refer [<! >! chan]]
               :clj
               [clojure.core.async :refer [<! >! go chan]])))

(defn basic-auth
  "Given a username and a password return an API token"
  [service username password]
  (restful :raw :get "/user/tokens?type=api" service
           #?(:cljs {:with-credentials? false
                     :basic-auth {:username username
                                  :password password}}
              :clj {:with-credentials? false
                    :basic-auth [username password]})
           (comp :token first :tokens)))

(defn login
  "Login the user using their username and password. Returns their new API token.
  If :token is present in `service`, it will get added to the Authorization header
  so that the anonymously saved lists gets merged into the users account."
  [service username password]
  (restful :post "/login" service {:username username
                                   :password password}))

(defn logout
  "Logout the authenticated user, invalidating the token provided in their
  Authorization header."
  [service]
  (restful :get "/logout" service {}))

(defn who-am-i?
  "Given a token return user information"
  [service token & [options]]
  (restful :get "/user/whoami" service (merge {:token token} options) :user))
