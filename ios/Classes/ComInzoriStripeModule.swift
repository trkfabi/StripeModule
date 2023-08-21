//
//  ComInzoriStripeModule.swift
//  Stripe
//
//  Created by Fabian Martinez
//  Copyright (c) 2023 Your Company. All rights reserved.
//

import UIKit
import TitaniumKit
import StripeIdentity
import StripeApplePay
import PassKit
import Foundation

/**
 
 Titanium Swift Module Requirements
 ---
 
 1. Use the @objc annotation to expose your class to Objective-C (used by the Titanium core)
 2. Use the @objc annotation to expose your method to Objective-C as well.
 3. Method arguments always have the "[Any]" type, specifying a various number of arguments.
 Unwrap them like you would do in Swift, e.g. "guard let arguments = arguments, let message = arguments.first"
 4. You can use any public Titanium API like before, e.g. TiUtils. Remember the type safety of Swift, like Int vs Int32
 and NSString vs. String.
 
 */

var client_secret = "";

@objc(ComInzoriStripeModule)
class ComInzoriStripeModule: TiModule {
    
    func moduleGUID() -> String {
        return "52d5c2bb-87aa-4fb8-b7e1-1508b149e4c4"
    }
    
    override func moduleId() -> String! {
        return "com.inzori.stripe"
    }
    
    override func startup() {
        super.startup()
        debugPrint("[DEBUG] \(self) loaded")
    }
    
    @objc(startVerification:)
    func startVerification(arguments: Array<Any>?) {
        print("[DEBUG] startVerification method")
        guard let arguments = arguments, let options = arguments[0] as? [String: Any] else { return }
        guard let callback: KrollCallback = options["onComplete"] as? KrollCallback else { return }
        
        let verificationSessionId = options["verificationSessionId"] as? String ?? ""
        let ephemeralKeySecret = options["ephemeralKeySecret"] as? String ?? ""
        
        let configuration = IdentityVerificationSheet.Configuration(
            brandLogo: UIImage(named: "logo")!
        )
        
        // Instantiate and present the sheet
        let verificationSheet = IdentityVerificationSheet(
            verificationSessionId: verificationSessionId,
            ephemeralKeySecret: ephemeralKeySecret,
            configuration: configuration
        )
        
        verificationSheet.present(from: TiApp.controller().topPresentedController(), completion: { result in
            switch result {
            case .flowCompleted:
                // The user has completed uploading their documents.
                // Let them know that the verification is processing.
                print("Verification Flow Completed!")
                callback.call([["success": true, "type": "status", "status":"flowCompleted", "message": "Verification Flow Completed!"] as [String : Any]], thisObject: self)
            case .flowCanceled:
                // The user did not complete uploading their documents.
                // You should allow them to try again.
                print("Verification Flow Canceled!")
                callback.call([["success": true, "type": "status", "status":"flowCanceled", "message": "Verification Flow Canceled!"] as [String : Any]], thisObject: self)
            case .flowFailed(let error):
                // If the flow fails, you should display the localized error
                // message to your user using error.localizedDescription
                print("Verification Flow Failed!")
                print(error.localizedDescription)
                callback.call([["success": true, "type": "status", "status":"flowFailed", "message": error.localizedDescription] as [String : Any]], thisObject: self)
            }
        })
    }
    
    // Payments
    @objc(startPayments:)
    func startPayments(arguments: Array<Any>?) {
        print("[DEBUG] startPayments method")
        guard let arguments = arguments, let options = arguments[0] as? [String: Any] else { return }
        guard let callback: KrollCallback = options["onComplete"] as? KrollCallback else { return }
        
        let publishableKey = options["publishableKey"] as? String ?? ""
        StripeAPI.defaultPublishableKey = publishableKey
        let isApplePaySupported = StripeAPI.deviceSupportsApplePay()

        callback.call([["success": true, "message": "Stripe payments initialized", "isApplePaySupported": isApplePaySupported] as [String : Any]], thisObject: self)
    }
    
    @objc(processPayment:)
    func processPayment(arguments: Array<Any>?) {
        guard let arguments = arguments, let options = arguments[0] as? [String: Any] else { return }
        
        let merchantIdentifier = options["merchantId"] as? String ?? "merchant.com.fluidtruck"
        let companyName = options["companyName"] as? String ?? "Fluid Truck"
        let amount = options["amount"] as? Double ?? 0
        let currency = options["currency"] as? String ?? "USD"
        let country = options["country"] as? String ?? "US"
        client_secret = options["clientSecret"] as? String ?? ""
                
        let paymentRequest = StripeAPI.paymentRequest(withMerchantIdentifier: merchantIdentifier, country: country, currency: currency)

        // Configure the line items on the payment request
        paymentRequest.paymentSummaryItems = [
            // The final line should represent your company;
            PKPaymentSummaryItem(label: companyName, amount: NSDecimalNumber(value: amount/100)),
        ]
        // Initialize an STPApplePayContext instance
        if let applePayContext = STPApplePayContext(paymentRequest: paymentRequest, delegate: self) {
            // Present Apple Pay payment sheet
            applePayContext.presentApplePay()
        } else {
            // There is a problem with your Apple Pay configuration
            fireEvent("fluid:payment_status", with: ["success": false, "cancelled": false, "message": "There was a problem processing payment"] as [String : Any])
        }
    }
    
    @objc(setClientSecret:)
    func setClientSecret(arguments: Array<Any>?) {
        guard let arguments = arguments, let options = arguments[0] as? [String: Any] else { return }
        client_secret = options["clientSecret"] as? String ?? ""
    }
}

extension ComInzoriStripeModule: ApplePayContextDelegate {

    func applePayContext(_ context: STPApplePayContext, didCreatePaymentMethod paymentMethod: StripeAPI.PaymentMethod, paymentInformation: PKPayment, completion: @escaping STPIntentClientSecretCompletionBlock) {
        // Call the completion block with the client secret or an error
        fireEvent("fluid:payment_status", with: ["success": true, "event": "didCreatePaymentMethod", "message": "Payment method created \(paymentMethod)"] as [String : Any])
        completion(client_secret, nil);
    }
    
    func applePayContext(_ context: STPApplePayContext, didCompleteWith status: STPApplePayContext.PaymentStatus, error: Error?) {
        switch status {
        case .success:
            // Payment succeeded, show a receipt view
            fireEvent("fluid:payment_status", with: ["success": true,"event": "didCompleteWith", "cancelled": false, "message": "Payment succeeded"] as [String : Any])
            break
        case .error:
            // Payment failed, show the error
            fireEvent("fluid:payment_status", with: ["success": false, "event": "didCompleteWith","cancelled": false, "message": "\(String(describing: error))"] as [String : Any])
            break
        case .userCancellation:
            // User canceled the payment
            fireEvent("fluid:payment_status", with: ["success": true, "event": "didCompleteWith","cancelled": true, "message": "User cancelled payment"] as [String : Any])
            break
        }
    }
}

// just for testing
func createPaymentIntent(url: String, amount: Decimal, currency: String, apiSecretKey: String) {
    // Prepare the request URL
    let url = URL(string: url)!
    
    // Prepare the request body parameters
    let parameters: [String: Any] = [
        "amount": amount,
        "currency": currency
    ]
    
    // Create the request
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("Bearer "+apiSecretKey, forHTTPHeaderField: "Authorization")
    request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
    
    // Set the request body data
    let bodyData = parameters
        .map { "\($0)=\($1)" }
        .joined(separator: "&")
    request.httpBody = bodyData.data(using: .utf8)
    
    // Perform the request
    let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
        if let error = error {
            print("Error: \(error)")
            return
        }
        
        guard let data = data else {
            print("No data received")
            return
        }
        
        // Process the response data
        let responseString = String(data: data, encoding: .utf8)
        print("Response: \(responseString ?? "")")
        
        // Parse and handle the response as needed
        // ...
        
    }
    
    task.resume()
}


