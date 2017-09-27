(ns mobile-patient.lib.services
  (:require [re-frame.core :refer [subscribe dispatch reg-event-fx reg-event-db]]
            [mobile-patient.lib.helper :as h]
            [mobile-patient.lib.interceptor :refer [validate-spec]]))


(defn reg-get-service [event-name db-path default-opts &
                       {:keys [accessor mutator] :or {accessor :entry
                                                      mutator identity}}]
  (reg-event-fx
   event-name
   validate-spec
   (fn [_ [_ extra-opts]]
     (let [base-url @(subscribe [:get-in [:config :base-url]])
           opts (merge default-opts extra-opts)
           data-path (keyword (str (name (first db-path)) "-data"))
           token @(subscribe [:get-in [:auth :id_token]])]
       (println opts)

       (dispatch [:assoc-in (concat db-path [:status]) :loading])
       (-> (js/fetch (str base-url (:uri opts) (h/parms->query (:params opts)))
                     (clj->js (merge {:redirect "manual"
                                      :method "GET"
                                      :headers (merge {"Content-Type" "application/json"}
                                                      {"Authorization" (str "Bearer " token)})}
                                     opts)))
           (.then #(.json %))
           (.then (fn [resp]
                    (dispatch [(keyword (str "success-" (name event-name)))
                               db-path
                               {:status :succeed
                                data-path (-> (js->clj resp :keywordize-keys true)
                                              accessor
                                              mutator)}])))
           (.catch #(dispatch [:assoc-in db-path {:status :failure
                                                  data-path (.-message %)}])))
       {})))

  (reg-event-db
   (keyword (str "success-" (name event-name)))
   validate-spec
   (fn [db [_ path data]]
     (assoc-in db path data))))
