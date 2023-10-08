(ns stockpile.auth)

(defn- is-authenticated?
  [request]
  (seq (:session request)))

(defn- get-role
  [request]
  (-> request
      :session
      :role))

(defn- is-admin?
  [request]
  (= (get-role request) "admin"))

(defn- is-editor?
  [request]
  (let [role (get-role request)]
    (or (= role "editor") (= role "admin"))))

(defn wrap-auth
  [handler]
  (fn [request]
    (if (is-authenticated? request)
      (handler request)
      {:status 401, :body {:error "Please sign in first"}})))

(defn wrap-auth-admin
  [handler]
  (fn [request]
    (if (is-admin? request)
      (handler request)
      {:status 401, :body {:error "Not authurized"}})))

(defn wrap-auth-editor
  [handler]
  (fn [request]
    (if (is-editor? request)
      (handler request)
      {:status 401, :body {:error "Not authurized"}})))