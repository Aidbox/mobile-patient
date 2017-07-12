(ns practitioner.routes
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.object :refer [getValueByKeys]]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.route-helpers :as rh]
            [practitioner.screen.patients :refer [PatientsScreen]]))

(defn dumb-component [text]
  (r/reactify-component (fn [] [ui/text text])))

(defn back-button [navigation]
  [ui/touchable-highlight {:style {:margin-left 10}
                           :on-press #(navigation.navigate "Patients")
                           :underlay-color color/grey}
   [ui/icon {:name "chevron-left" :size 24 :color color/pink}]])

(defn tabs-nav-opts
  ([title]
   (fn [props]
     #js{:title title
         :headerLeft (r/as-element [back-button props.navigation])
         :headerTitleStyle #js{:paddingLeft 10
                               :fontSize 19
                               :fontWeight "bold"
                               :color color/grey}
         })))

(def tabs-routes
  (let []
    (ui/TabNavigator
     (clj->js
      {
       "Meds" {:screen (dumb-component "Meds")
               :navigationOptions (tabs-nav-opts "Meds")}

       "Vitals" {:screen (dumb-component "Chats")
                 :navigationOptions (tabs-nav-opts "Vitals")}

       "Chats" {:screen (dumb-component "Chats")
                :navigationOptions (tabs-nav-opts "Chats")}

       })
     (clj->js {:tabBarPosition "top"
               :tabBarOptions {:activeTintColor "#fff"
                               :inactiveTintColor "#555"
                               :style {:backgroundColor color/light-grey}
                               :tabStyle {:backgroundColor color/grey}
                               :labelStyle {}}}))))

(def drawer-routes
  (ui/DrawerNavigator
   (clj->js
    {"Patients"
     {:screen (rh/stack-navigator
               {"PatientsList"
                {:screen (r/reactify-component PatientsScreen)
                 :navigationOptions (rh/drawer-nav-opts "Patients")}})}

     "About App"
     {:screen (rh/stack-navigator
               {"About App"
                {:screen (r/reactify-component (fn [] [ui/text "About app"]))
                 :navigationOptions (rh/drawer-nav-opts "About App")}})}

     "PatientWrapper"
     {:screen (rh/stack-navigator
               {"Patient"
                {:screen tabs-routes
                 :navigationOptions
                 (fn [props]
                   ;;(print (getValueByKeys props "navigation" "state" "params" "patientid"))
                   #js {:title (getValueByKeys props "navigation" "state" "params" "patientid")})
                 }})}})
   (clj->js
    {:drawerWidth 300
     :contentComponent
     (fn [props]
       (r/as-element [rh/drawer-content props]))})))



;; On navigation chnage handler
(defn on-navigation-callback [prev-state new-state action]
  (let [action (js->clj action :keywordize-keys true)
        route-name (:routeName action)]
    (case route-name
      ;;"Vitals Signs" (rf/dispatch [:on-vitals-sign-screen])
      nil)))
