// This is a test harness for your module
// You should do something interesting in this harness
// to test out the module and to provide instructions
// to users on how to use it by example.


// open a single window
const win = Ti.UI.createWindow();
const label = Ti.UI.createLabel();
win.add(label);
win.open();

// TODO: write your module tests here
import stripe  from 'com.inzori.stripe';
Ti.API.info("module is => " + stripe);

label.text = stripe.example();

Ti.API.info("module exampleProp is => " + stripe.exampleProp);
stripe.exampleProp = "This is a test value";

if (Ti.Platform.name == "android") {
	const proxy = stripe.createExample({
		message: "Creating an example Proxy",
		backgroundColor: "red",
		width: 100,
		height: 100,
		top: 100,
		left: 150
	});

	proxy.printMessage("Hello world!");
	proxy.message = "Hi world!.  It's me again.";
	proxy.printMessage("Hello world!");
	win.add(proxy);
}
