(ns practitioner.routes
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.route-helpers :as rh]
            [practitioner.screen.patients :refer [PatientsScreen]]))

(def drawer-routes
  (ui/DrawerNavigator
   (clj->js
    {"Patients" {:screen (rh/stack-navigator
                          {"PatientsList" {:screen (r/reactify-component PatientsScreen)
                                           :navigationOptions (rh/drawer-nav-opts "Patients")}})}
    "About App" {:screen (rh/stack-navigator
                          {"About App" {:screen (r/reactify-component (fn [] [ui/text "About app"]))
                                        :navigationOptions (rh/drawer-nav-opts "About App")}})}})
   (clj->js
    {:drawerWidth 300
     :contentComponent (fn [props]
                         (r/as-element [rh/drawer-content props]))})))

;; On navigation chnage handler
(defn on-navigation-callback [prev-state new-state action]
  (let [action (js->clj action :keywordize-keys true)
        route-name (:routeName action)]
    (case route-name
      ;;"Vitals Signs" (rf/dispatch [:on-vitals-sign-screen])
      nil)))
