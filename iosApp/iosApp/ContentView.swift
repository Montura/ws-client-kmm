import SwiftUI
import shared

struct ContentView: View {
	let greeting = Greeting()
    let client = WebClient()
    
	@State var greet = "Loading..."

    // mapping to Kotlin suspend function
    // https://mmmnnnmmm.com/kotlin-kmm-swift-ios-suspend-functions.html
//    class MySuspendFunction: KotlinSuspendFunction1 {
//        func invoke(p1: Any?) async throws -> Any? {
//            return "Please exclaim this remark"
//        }
//    }
    
	func load() {
        client.run(host: "192.168.12.133", port: 8080, path: "/dxfeed-webservice/cometd")
        
        
//        greeting.onWebSocket(host: "192.168.12.69", port: 8080, path: "/chat") {
//            return "aaa iOS"
//        } completionHandler: { result, error in
//            
//        }
//
////        greeting.onConnect { result, error in
////            print(result)
////        }
//        greet += greeting.greeting() + "\n\n"
//        greeting.getHtml { result, error in
//            if let result = result {
//                self.greet += result
//            } else if let error = error {
//                greet += "Error: \(error)"
//            }
//        }
    }

	var body: some View {
		Text(greet).onAppear() {
            load()
        }
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
