import SwiftUI
import shared


func delayWithSeconds(_ seconds: Double, completion: @escaping () -> ()) {
    DispatchQueue.main.asyncAfter(deadline: .now() + seconds) {
        completion()
    }
}

struct ContentView: View {
	@State var greet = "Loading..."
    
	func load() {
	    // todo: set up VPN GlobalProtect on iPhone
        let api = DxFeedApi(host: "demo.dxfeed.com", port: 7521, path: "/wapi/rsocket");

        
        let eventTypes = ["Quote"]
        let sub = api.createSubscription(eventTypes: eventTypes) {
            data in WebClientKt.logWithThreadName(msg: data.json ?? "null")
        }
        
        sub.addSymbols(symbols: ["AAPL"])
        sub.addSymbols(symbols: ["MSFT"])
        
        delayWithSeconds(10) {
            sub.removeSymbols(symbols: ["AAPL"])
        }

        
        delayWithSeconds(10.0) {
            sub.remove()
        }
        
    }

	var body: some View {
		Text("Loading...").onAppear() {
            load()
        }
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
