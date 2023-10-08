(ns stockpile.routes
  (:require [stockpile.handlers :as handle]
            [schema.core :as s]
            [ring.middleware.cors :refer [wrap-cors]]
            [stockpile.auth :refer
             [wrap-auth wrap-auth-admin]]))

(def users-routes
  ["/users" {:name ::users}
   ["" {:get {:handler handle/users}}]
   ["/register"
    {:name ::register,
     :post {:parameters {:body {:username s/Str,
                                :password s/Str,
                                :email s/Str,
                                :role s/Str}},
            :handler handle/register}}]])

(def roles-routes
  ["/roles" {:name ::roles}
   ["" {:get {:handler handle/roles}}]
   ["/create"
    {:name ::create-role,
     :post {:parameters {:body {:role s/Str}},
            :handler handle/create-role}}]])

(def admin-routes
  ["/admin"
   {:name ::admin,
    :middleware [wrap-auth wrap-auth-admin]}
   users-routes roles-routes])

(def auth-routes
  ["/auth" {:name ::auth}
   ["/login"
    {:name ::login,
     :post {:parameters {:body {:email s/Str,
                                :password s/Str}},
            :handler handle/login}}]
   ["/logout"
    {:name ::logout,
     :post {:handler handle/logout}}]])

(def api-routes
  ["/api" {:name ::api} admin-routes auth-routes])
