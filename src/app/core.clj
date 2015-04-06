(ns app.core
  (:require
    [ring.adapter.jetty :refer [run-jetty]]
    [compojure.handler :as handler]
    [ring.util.response :refer [response]]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [compojure.core :refer :all]
    [compojure.route :as route]
    [clojure.java.io :refer [output-stream]]
    [clojure.data.json :as json]
    [app.wav :refer [sample-and-save]]
    [app.voice :refer [analyse]]
    [ring.util.response :refer [response redirect]]
    ))

(defroutes api-routes
  (POST "/" [] (fn [x]
    (let [data (flatten (:body x))
          [id path] (sample-and-save data)]
      (let [text (analyse path)]
        (response {"ok" id
                   "text" text}))))))

(defroutes app-routes
  (GET "/" [] (redirect "/index.html"))
  (route/resources "/"))

(defroutes combined-routes
  (wrap-json-response
    (wrap-json-body api-routes
      {:keywords? true :bigdecimals? true}))
  app-routes)

(def app combined-routes)

(defn -main [& args]
  (run-jetty app {:port 3000}))
