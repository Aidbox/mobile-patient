(ns practitioner.routes
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.object :refer [getValueByKeys]]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.route-helpers :as rh]
            [mobile-patient.routes :refer [routes]]
            [practitioner.drawer :refer [Drawer]]))

(defn dumb-component [text]
  (r/reactify-component (fn [] [ui/text text])))

(def drawer-routes
  (ui/DrawerNavigator
   routes

   (clj->js
    {:initialRouteName "Patients"
     :drawerWidth 250
     :contentComponent
     (fn [props]
       (r/as-element [Drawer props]))})))


;; On navigation chnage handler
(defn on-navigation-callback [prev-state new-state action]
  (let [action (js->clj action :keywordize-keys true)
        route-name (:routeName action)
        params (:params action)]
    #_(case route-name
      "Patient" (do
                  (rf/dispatch-sync [:spinner :load-patient-data true])
                  (rf/dispatch [:get-patient-data
                              @(rf/subscribe [:get-patient-ref-by-id (:patientid params)])
                              :get-medication-statements]))
      nil)))
