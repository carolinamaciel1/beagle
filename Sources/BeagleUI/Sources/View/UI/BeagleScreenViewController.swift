//
//  BeagleScreenViewController.swift
//  BeagleUI
//
//  Created by Eduardo Sanches Bocato on 09/10/19.
//  Copyright © 2019 Daniel Tes. All rights reserved.
//

import UIKit

extension BeagleScreenViewController {
    public enum ScreenType {
        case remote(URL)
        case declarative(Widget)
    }
}

public protocol BeagleScreenViewControllerDelegate: AnyObject {

    func beagleScreenViewController(
        _ controller: BeagleScreenViewController,
        didFailToLoadWithError error: Error
    )
}

public class BeagleScreenViewController: UIViewController {
    
    // MARK: - Dependencies
    
    let screenType: ScreenType
    let flexConfigurator: FlexViewConfiguratorProtocol
    let viewBuilder: BeagleViewBuilder
    let serverDrivenScreenLoader: ServerDrivenScreenLoader
    let actionExecutor: ActionExecutor
    
    // MARK: - Properties
    
    weak var delegate: BeagleScreenViewControllerDelegate?
    
    // MARK: - Initialization
    
    public convenience init(
        screenType: ScreenType,
        viewBuilder: BeagleViewBuilder = BeagleViewBuilding(),
        serverDrivenScreenLoader: ServerDrivenScreenLoader = ServerDrivenScreenLoading()
    ) {
        self.init(
            screenType: screenType,
            flexConfigurator: FlexViewConfigurator(),
            viewBuilder: viewBuilder,
            serverDrivenScreenLoader: serverDrivenScreenLoader,
            actionExecutor: ActionExecuting()
        )
    }
    
    init(
        screenType: ScreenType,
        flexConfigurator: FlexViewConfiguratorProtocol,
        viewBuilder: BeagleViewBuilder,
        serverDrivenScreenLoader: ServerDrivenScreenLoader,
        actionExecutor: ActionExecutor
    ) {
        self.screenType = screenType
        self.flexConfigurator = flexConfigurator
        self.viewBuilder = viewBuilder
        self.serverDrivenScreenLoader = serverDrivenScreenLoader
        self.actionExecutor = actionExecutor
        super.init(nibName: nil, bundle: nil)
    }
    
    @available(*, unavailable)
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: - Lifecycle
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        setupView()
        loadScreen()
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: animated)
    }
    
    // MARK: - Private Functions
    
    private func setupView() {
        view.backgroundColor = .white
    }
    
    private func loadScreen() {
        switch screenType {
        case let .declarative(widget):
            loadDeclarativeScreenWithRootWidget(widget, context: self)
        case let .remote(url):
            loadScreenFromURL(url)
        }
    }
    
    // MARK: - Declarative Screen Loading
    
    private func loadDeclarativeScreenWithRootWidget(_ widget: Widget, context: BeagleContext) {
        let declarativeView = viewBuilder.buildFromRootWidget(widget, context: context)
        setupWidgetView(declarativeView)
    }
    
    // MARK: - Remote Screen Loading
    
    private func loadScreenFromURL(_ url: URL) {
        view.showLoading(.whiteLarge)
        serverDrivenScreenLoader.loadScreen(from: url, context: self) { [weak self] result in
            self?.view.hideLoading()
            switch result {
            case let .success(view):
                self?.setupWidgetView(view)
            case let .failure(error):
                self?.handleError(error)
            }
        }
    }
    
    // MARK: - View Setup
    
    private func setupWidgetView(_ widgetView: UIView) {
        view.addSubview(widgetView)
        widgetView.frame = CGRect(x: 0, y: 0, width: view.frame.width, height: view.frame.height)
        flexConfigurator.applyYogaLayout(to: widgetView, preservingOrigin: true)
    }
    
    // MARK: - Error Handling
    
    func handleError(_ error: Error) {
        delegate?.beagleScreenViewController(self, didFailToLoadWithError: error)
    }
    
}
