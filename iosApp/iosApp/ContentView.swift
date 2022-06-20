import SwiftUI
import shared


func delayWithSeconds(_ seconds: Double, completion: @escaping () -> ()) {
    DispatchQueue.main.asyncAfter(deadline: .now() + seconds) {
        completion()
    }
}

struct ContentView: View {
	let greeting = Greeting()

	@State var greet = "Loading..."
    
	func load() {
        let api = DxFeedApi(host: "208.93.103.3", port: 7521, path: "/wapi/rsocket");

        
        let eventTypes = ["Quote"]
        let sub = api.createSubscription(eventTypes: eventTypes) {
            data in WebClientKt.logWithThreadName(msg: data.json ?? "null")
        }
        
        sub.addSymbols(symbols: ["AAPL"])
        sub.addSymbols(symbols: ["MSFT"])

        delayWithSeconds(10) {
            sub.removeSymbols(symbols: ["AAPL"])
        }

        
        delayWithSeconds(15) {
            sub.remove()
        }
        
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
