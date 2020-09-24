//
//  ViewController.swift
//  JsIntegrationDemo
//
//  Copyright © 2020 VOYAGE GROUP, INC. All rights reserved.
//

import UIKit
import WebKit
import AdSupport

class ViewController: UIViewController {
    
    private let APP_IN_VIEW_DOMAIN = "voyagegroup.github.io"
    
    @IBOutlet private weak var webView: WKWebView! {
        didSet {
            webView.navigationDelegate = self
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let request = URLRequest(url: URL(string: "https://voyagegroup.github.io/FluctSDK-Hosting/js-sample/ios.html")!)
        webView.load(request)
    }
}

extension ViewController: WKNavigationDelegate {
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        
        let groupId = "1000123947"
        let unitId = "1000214356"
        
        let js =
        """
        var fluctAdScript = fluctAdScript || {};
        fluctAdScript.cmd = fluctAdScript.cmd || [];
        fluctAdScript.cmd.push(function (cmd) {
        cmd.setConfig({dlvParams: {\"ifa\":\"\(idfa)\",\"lmt\":\"\(lmt)\",\"bundle\":\"\(bundle)\"}});
        cmd.loadByGroup(\"\(groupId)\");
        cmd.display(\".fluct-unit-\(unitId)\", \"\(unitId)\");
        });
        """
        
        webView.evaluateJavaScript(js, completionHandler: nil)
    }
    
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        // リンククリックによるリクエストか判定
        let isLinkClick = navigationAction.navigationType == .linkActivated
        
        // 外部Safariではなく、アプリ内で遷移させたいURLか判定
        let isAppInViewDomain = navigationAction.request.url?.host == APP_IN_VIEW_DOMAIN
        
        if isLinkClick && !isAppInViewDomain, let url = navigationAction.request.url {
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
            decisionHandler(.cancel)
            return
        }
        decisionHandler(.allow)
    }
}

extension ViewController {
    private var idfa: String {
        return ASIdentifierManager().advertisingIdentifier.uuidString
    }
    
    private var lmt: String {
        return ASIdentifierManager().isAdvertisingTrackingEnabled ? "0" : "1"
    }
    private var bundle: String {
        return Bundle.main.bundleIdentifier ?? ""
    }
}
