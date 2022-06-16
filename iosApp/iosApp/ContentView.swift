import SwiftUI
import shared


struct ContentView: View {
	let greeting = Greeting()
    let client = WebClient(clientKt: WebClientKt.httpClient())
    
	@State var greet = "Loading..."
    
	func load() {
        let api = DxFeedApi(httpClient: WebClientKt.httpClient());
        
        
        let eventTypes = ["Quote"]
        let sub = api.createSubscription(eventTypes: eventTypes) {
            data in print(data.json ?? "null")
        }
        
        sub.addSymbols(symbols: ["AAPL"])
        sub.addSymbols(symbols: ["MSFT"])
    

        sub.removeSymbols(symbols: ["AAPL"])

        sub.remove()
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
