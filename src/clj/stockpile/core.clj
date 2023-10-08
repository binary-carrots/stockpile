(ns stockpile.core
  (:gen-class)
  (:require [clojure.pprint]
            [jdbc-ring-session.cleaner :refer
             [start-cleaner stop-cleaner]]
            [jdbc-ring-session.core :refer [jdbc-store]]
            [muuntaja.core :as m]
            [reitit.coercion.schema]
            [reitit.ring :as ring]
            [reitit.ring.coercion :refer
             [coerce-exceptions-middleware coerce-request-middleware
              coerce-response-middleware]]
            [reitit.ring.middleware.exception :refer
             [exception-middleware]]
            [reitit.ring.middleware.muuntaja :refer
             [format-negotiate-middleware format-request-middleware
              format-response-middleware]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.session :refer
             [wrap-session]]
            [ring.middleware.session-timeout :refer
             [wrap-idle-session-timeout]]
            [stockpile.db :refer [mysql-db]]
            [stockpile.handlers :as handle]
            [stockpile.routes :refer [api-routes]]))

(def app
  (ring/ring-handler
   (ring/router
    [api-routes
     ["assets/*"
      (ring/create-resource-handler
       {:root "public/assets"})]]
    {:data
     {:coercion reitit.coercion.schema/coercion,
      :muuntaja m/instance,
      :middleware
      ;; Middleware chain runs from bottom to
      ;; top
      [[wrap-cors
        :access-control-allow-origin #".*"
        :access-control-allow-methods [:get :post :put :delete]]
       [wrap-session
        {:cookie-attrs {:secure true},
         :store (jdbc-store mysql-db)}]
       [wrap-idle-session-timeout
        {:timeout-handler handle/timeout,
         :timeout 600}]
       format-negotiate-middleware
       format-response-middleware
       exception-middleware
       format-request-middleware
       coerce-exceptions-middleware
       coerce-request-middleware
       coerce-response-middleware]}})
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler
     {:not-found (constantly {:status 404,
                              :body
                              "not found"})}))))

(defonce server (atom nil))
(defonce cleaner (atom nil))

(def reloadable-server (wrap-reload #'app))

(defn start-server
  []
  (reset! server (run-jetty #'reloadable-server
                            {:port 3000,
                             :join? false}))
  (reset! cleaner (start-cleaner mysql-db
                                 {:interval 10})))

(defn stop-server
  []
  (when-some [s @server]
    (.stop s)
    (reset! server nil))
  (when-some [sc @cleaner]
    (stop-cleaner sc)
    (reset! cleaner nil)))

(defn -main
  [& args]
  (run-jetty #'app {:port 3000, :join? true})
  (println "server was started on port 3000"))

(comment
  (start-server)
  (stop-server))
