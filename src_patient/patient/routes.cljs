(ns patient.routes
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.route-helpers :as rh]
            [mobile-patient.routes :refer [routes]]
            [patient.drawer :refer [Drawer]]))

(defn dumb-component [text]
  (r/reactify-component (fn [] [ui/text text])))

(def drawer-routes
  (ui/DrawerNavigator
   routes
   (clj->js
    {:drawerWidth 300
     :contentComponent (fn [props]
                         (r/as-element [Drawer props]))})))

;; On navigation chnage handler
(defn on-navigation-callback [prev-state new-state action]
  (let [action (js->clj action :keywordize-keys true)
        route-name (:routeName action)]
    #_(case route-name
      ;;"Medications" (rf/dispatch [:get-medication-statements])
      "Vitals Signs" (rf/dispatch [:on-vitals-sign-screen])
      "Contacts" (rf/dispatch [:load-contacts])
      nil)))
