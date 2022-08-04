# ws-client-kmm experiment
Kotlin Mobile Multiplatform 

# Ktor client in a Kotlin Multiplatform Mobile application

The project created in the [Creating a cross-platform mobile application](https://ktor.io/docs/getting-started-ktor-client-multiplatform-mobile.html) tutorial.


## How to run
### [Set up the environment to work with CocoaPods](https://kotlinlang.org/docs/native-cocoapods.html#set-up-the-environment-to-work-with-cocoapods)
  * `sudo gem install cocoapods`
  * If you use Kotlin prior to version 1.7.0:
    * `sudo gem install cocoapods-generate`
### Build 
  * `$ ./gradlew build`
  * `$ cd iosApp`
  * `pod install`
  * open `iosApp.xcworkspace`
  * tap `Build`
