(ns mobile-patient.lib.services
  (:require [re-frame.core :refer [subscribe dispatch reg-event-fx]]
            [mobile-patient.lib.helper :as h]
            [mobile-patient.events :refer [validate-spec]]))


(defn reg-get-service [event-name db-path default-opts &
                       {:keys [accessor mutator] :or {accessor :entry
                                                      mutator identity}}]
  (reg-event-fx
   event-name
   validate-spec
   (fn [_ [_ extra-opts]]
     (let [base-url @(subscribe [:get-in [:config :base-url]])
           opts (merge default-opts extra-opts)]
       (dispatch [:assoc-in (concat db-path [:status]) :loading])
       (-> (js/fetch (str base-url (:uri opts) (h/parms->query (:params opts)))
                     (clj->js (merge {:redirect "manual"
                                     :method "GET"
                                     :headers {"Content-Type" "application/json"}}
                                     opts)))
           (.then #(.json %))
           (.then #(dispatch [:assoc-in db-path {:status :succeed
                                                 :data (-> (js->clj % :keywordize-keys true)
                                                           accessor
                                                           mutator)}]))
           (.catch #(dispatch [:assoc-in db-path {:status :failure
                                                  :data (.-message %)}])))
       {}))))
