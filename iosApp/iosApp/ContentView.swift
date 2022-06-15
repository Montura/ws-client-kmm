import SwiftUI
import shared

class IosWsEventListener : WsEventHandler {
    var client: WebClient? = nil
    
    private func onWebSocketOpen() async throws {
        try await client?.sendMessage(messageSupplier: { clientId in
            WebClientUtil.companion.createConnectMessage(clientId: clientId)
        })

        try await client?.sendMessage(messageSupplier: { clientId in
            WebClientUtil.companion.createSubscriptionMessage(clientId: clientId, eventTypes: ["Quote"], symbols: ["AAPL"])
        })
   }

   private func onWebSocketConnect(success: Bool) {
       WebClientKt.logWithThreadName(msg: "USER_HANDLER:")
       if (success) {
           WebClientKt.logWithThreadName(msg: "\tConnection is established!")
       } else {
           WebClientKt.logWithThreadName(msg: "\tCan't establish a connection!")
       }
   }

   private func onSubscribe(success: Bool) {
       WebClientKt.logWithThreadName(msg: "USER_HANDLER:")
       if (success) {
           WebClientKt.logWithThreadName(msg: "\tConnection is established!")
       } else {
           WebClientKt.logWithThreadName(msg: "\tCan't establish a connection!")
       }
   }

   private func onData(json: KotlinMutableDictionary<NSString, AnyObject>) {
       WebClientKt.logWithThreadName(msg: "USER_HANDLER: todo -> process data")
   }
    
    func processIncomingMessage(client: WebClient, msg: String, json: KotlinMutableDictionary<NSString, AnyObject>) async throws -> KotlinUnit {
        self.client = client
        WebClientKt.logWithThreadName(msg: "RECV: $msg")
        
        let channel = JsonUtilKt.channel(json)
        switch (channel) {
        case WebClientUtil.companion.HANDSHAKE_CHANNEL:
            try await onWebSocketOpen()
        case WebClientUtil.companion.CONNECT_CHANNEL:
            onWebSocketConnect(success: JsonUtilKt.booleanValue(json, channelAsKey: WebClientUtil.companion.SUCCESSFUL_KEY))
        case WebClientUtil.companion.SERVICE_SUB_CHANNEL:
            onSubscribe(success: JsonUtilKt.booleanValue(json, channelAsKey: WebClientUtil.companion.SUCCESSFUL_KEY))
        case WebClientUtil.companion.SERVICE_DATA_CHANNEL:
            onData(json: json)
        default:
            WebClientKt.logWithThreadName(msg: "Unkwon channel: " + channel)
        }
        return KotlinUnit()
    }
}

struct ContentView: View {
	let greeting = Greeting()
    let client = WebClient(clientKt: WebClientKt.httpClient())
    
	@State var greet = "Loading..."

    // mapping to Kotlin suspend function
    // https://mmmnnnmmm.com/kotlin-kmm-swift-ios-suspend-functions.html
//    class MySuspendFunction: KotlinSuspendFunction1 {
//        func invoke(p1: Any?) async throws -> Any? {
//            return "Please exclaim this remark"
//        }
//    }
    
	func load() {
        let listener = IosWsEventListener();
        
        client.run(host: "tools.dxfeed.com", port: 0, path: "/webservice/cometd", userListener: listener)
        
        
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
