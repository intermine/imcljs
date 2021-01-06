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

(defn change-password
  "Changes the password of an authenticated user."
  [service old-password new-password]
  (restful :post "/user" service {:oldPassword old-password
                                  :newPassword new-password}))

(defn register
  "Register a new user account."
  [service username password & [options]]
  (let [params {:name username :password password}]
    (restful :post "/users" service (merge params options) :user)))

(defn deregistration
  "Creates a deregistration token which is to be passed to `delete-account`."
  [service & [options]]
  (restful :post "/user/deregistration" service options :token))

(defn delete-account
  "Takes a `deregistration` token to delete the authenticated user."
  [service deregistration-token & [options]]
  (let [params {:deregistrationToken deregistration-token}]
    (restful :delete "/user" service (merge params options))))

(defn oauth2authenticator
  "Commence authentication for logging in using OAuth 2.0 with specified
  provider.  Will return a URL to redirect to the external login page.
  Remember to append a `redirect_uri` parameter to the URL before redirecting.
  This should be an endpoint which will be redirected to after signing in at
  the third-party, passing parameters required for the `oauth2callback`.
  Note that the redirect URL might be checked against a whitelist."
  [service provider & [options]]
  (let [params {:provider provider}]
    (restful :get "/oauth2authenticator" service (merge params options) :link)))

(defn oauth2callback
  "Complete authentication for logging in using OAuth 2.0. Requires parameters
  state and code, which are received when redirecting back from the external
  login service in `oauth2authenticator`, in addition to provider which should
  be identical to the one passed to `oauth2authenticator`."
  [service & [options]]
  (restful :get "/oauth2callback" service options))

(defn request-password-reset
  "Sends a password reset email to the user containing a specified redirectUrl
  with a token appended as a query string."
  [service email redirectUrl & [options]]
  (let [params {:email email :redirectUrl redirectUrl}]
    (restful :get "/user/requestpswreset" service (merge params options))))

(defn password-reset
  "Reset the user's password using the token received from
  `request-password-reset` and newPassword."
  [service newPassword pswResetToken & [options]]
  (let [params {:newPassword newPassword :pswResetToken pswResetToken}]
    (restful :put "/user/pswreset" service (merge params options))))
