(ns mobile-patient.routes
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.screen.login :refer [LoginScreen]]
            [mobile-patient.screen.demographics :refer [DemographicsScreen]]
            [mobile-patient.screen.vitals :refer [VitalsScreen]]
            [mobile-patient.screen.meds :refer [MedsScreen]]
            [mobile-patient.screen.chat :refer [ChatsScreen ChatScreen]]
            [mobile-patient.screen.settings :refer [SettingsScreen]]
            [mobile-patient.screen.contacts :refer [ContactsScreen]]))


(def react-navigation (js/require "react-navigation"))

(def StackNavigator (.-StackNavigator react-navigation))
(def TabNavigator (.-TabNavigator react-navigation))

(def plus-img (js/require "./images/plus-circle.png"))
(def settings-img (js/require "./images/settings.png"))



;; NAVIGATION BUTTONS

(defn logout-button [navigation]
  [ui/link {:title "Log Out"
              :on-press #(navigation.navigate "Login")}])

(defn add-chat-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Contacts")}
   [ui/icon {:name "add-circle" :size 36 :color color/pink :margin-right 8}]])

(defn settings-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Settings")}
   [ui/icon {:name "settings" :size 36 :color color/grey :margin-right 8}]])

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

     "Chats"   {:screen (r/reactify-component ChatsScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Chats"
                                         :headerRight (r/as-element [chat-buttons props.navigation])})}})
   (clj->js
    {:tabBarPosition "bottom"
     :tabBarOptions {:activeTintColor "#fff"
                     :inactiveTintColor color/grey
                     :style {:backgroundColor color/light-grey}
                     :tabStyle {:backgroundColor color/grey}
                     :labelStyle {}}
     :initialRouteName "Chats" })))      ;delete


(def routes
  (StackNavigator
   (clj->js
    {"Login" {:screen (r/reactify-component LoginScreen)
              :navigationOptions {:header nil}}

     "Demographics" {:screen (r/reactify-component DemographicsScreen)
                     :navigationOptions {:title "Demographics"}}

     "Tabs" {:screen tab-routes}

     "Settings" {:screen (r/reactify-component SettingsScreen)
                 :headerTintColor color/grey
                 :navigationOptions {:title "Settings"}}

     "Chat" {:screen (r/reactify-component ChatScreen)
             :navigationOptions
             (fn [props]
               (let [chat-name (-> props .-navigation .-state .-params (aget "chat-name"))]
                 #js {:title chat-name}))}

     "Contacts" {:screen (r/reactify-component ContactsScreen)
                 :navigationOptions {:title "Contacts"}}

     })
   (clj->js
    {:initialRouteName "Login"
     :navigationOptions {:headerTintColor color/grey}
     :headerMode :screen})))
