import SwiftUI
import shared

struct ContentView: View {
	let greeting = Greeting()

	@State var greet = "Loading..."

	func load() {
        greeting.onConnect { result, error in
            print(result)
        }
        greet += greeting.greeting() + "\n\n"
        greeting.getHtml { result, error in
            if let result = result {
                self.greet += result
            } else if let error = error {
                greet += "Error: \(error)"
            }
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