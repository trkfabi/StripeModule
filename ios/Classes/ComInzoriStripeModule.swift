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
       
    @objc(initStripe:)
    func initStripe(arguments: Array<Any>?) {
        print("[DEBUG] initStripe method")
        guard let arguments = arguments, let options = arguments[0] as? [String: Any] else { return }
        guard let callback: KrollCallback = options["onComplete"] as? KrollCallback else { return }
        
        let url = options["url"] as? String ?? ""
        
        // Make request to your verification endpoint
        var urlRequest = URLRequest(url: URL(string: url)!)
        urlRequest.httpMethod = "POST"

        let task = URLSession.shared.dataTask(with: urlRequest) { [weak self] data, response, error in
            DispatchQueue.main.async { [weak self] in

            guard error == nil,
                let data = data,
                let responseJson = try? JSONDecoder().decode([String: String].self, from: data),
                let verificationSessionId = responseJson["id"],
                let ephemeralKeySecret = responseJson["ephemeral_key_secret"] else {
                // Handle error
                print(error as Any)
                callback.call([["success": false, "type": "log", "message": error?.localizedDescription as Any] as [String : Any]], thisObject: self)
                return
             }
                
                callback.call([["success": true, "message": "Sdk initialized", "type": "log"] as [String : Any]], thisObject: self)
            
                
                ///
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
                ///
                
           }
         }
         task.resume()
    }
    
}
