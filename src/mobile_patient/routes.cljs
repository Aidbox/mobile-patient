(ns mobile-patient.routes
  (:require [reagent.core :as r]
            [mobile-patient.ui :as ui]
            [mobile-patient.screen.login :refer [LoginScreen]]
            [mobile-patient.screen.demographics :refer [DemographicsScreen]]
            [mobile-patient.screen.vitals :refer [VitalsScreen]]
            [mobile-patient.screen.meds :refer [MedsScreen]]
            [mobile-patient.events :refer [ChatScreen]]
            [mobile-patient.screen.settings :refer [SettingsScreen]]
            [mobile-patient.screen.contacts :refer [ContactsScreen]]))


(def react-navigation (js/require "react-navigation"))

(def StackNavigator (.-StackNavigator react-navigation))
(def TabNavigator (.-TabNavigator react-navigation))

(def plus-img (js/require "./images/plus-circle.png"))
(def settings-img (js/require "./images/settings.png"))



;; NAVIGATION BUTTONS

(defn logout-button [navigation]
  [ui/button {:title "LogOut"
              :on-press #(navigation.navigate "Login")}])

(defn add-chat-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Contacts")}
   [ui/icon {:name "add-circle" :size 36 :color "#FF485C" :margin-right 8}]])

(defn settings-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Settings")}
   [ui/icon {:name "settings" :size 36 :color "#000" :margin-right 8}]])

(defn chat-buttons [navigation]
  [ui/view {:flex-direction :row :justify-content :space-around :width 86}
   [settings-button navigation]
   [add-chat-button navigation]])


;; NAVIGATION ROUTES

(def tab-routes
  (TabNavigator
   (clj->js
    {"Vitals" {:screen (r/reactify-component VitalsScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Vitals"
                                         :headerRight (r/as-element [logout-button props.navigation])})}

     "Meds"   {:screen (r/reactify-component MedsScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Meds"
                                         :headerRight (r/as-element [logout-button props.navigation])})}

     "Chat"   {:screen (r/reactify-component ChatScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Chat"
                                         :headerRight (r/as-element [chat-buttons props.navigation])})}})
   (clj->js
    {:tabBarPosition "bottom"
     :initialRouteName "Chat" })))      ;delete


(def routes
  (StackNavigator
   (clj->js
    {"Login" {:screen (r/reactify-component LoginScreen)
              :navigationOptions {:header nil}}

     "Demographics" {:screen (r/reactify-component DemographicsScreen)
                     :navigationOptions {:title "Demographics"}}

     "Tabs" {:screen tab-routes}

     "Settings" {:screen (r/reactify-component SettingsScreen)
                 :navigationOptions {:title "Settings"}}

     "Contacts" {:screen (r/reactify-component ContactsScreen)
                 :navigationOptions {:title "Contacts"}}

     })
   (clj->js
    {:initialRouteName "Tabs";; "Login"
     :headerMode :screen})))
