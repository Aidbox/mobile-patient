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
    {;;:initialRouteName "Chats" ;;for dev
     :drawerWidth 250
     :contentComponent (fn [props]
                         (r/as-element [Drawer props]))})))
