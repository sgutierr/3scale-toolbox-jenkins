#!groovy

package com.redhat

class ThreescaleService {
  OpenAPI2 openapi
  List<ApplicationPlan> applicationPlans
  ToolboxConfiguration toolbox
  ThreescaleEnvironment environment

  void importOpenAPI() {
    Util util = new Util()

    // Compute the target system_name
    this.environment.targetSystemName = (this.environment.environmentName != null ? "${this.environment.environmentName}_" : "") + this.environment.baseSystemName + "_${this.openapi.majorVersion}"

    def baseName = basename(this.openapi.filename)
    def globalOptions = toolbox.getGlobalToolboxOptions()
    def commandLine = [ "3scale", "import", "openapi" ] + globalOptions + [ "-t", this.environment.targetSystemName, "-d", this.toolbox.destination, "/artifacts/${baseName}" ]
    if (this.environment.stagingPublicBaseURL != null) {
      commandLine += "--staging-public-base-url=${stagingPublicBaseURL}"
    }
    if (this.environment.productionPublicBaseURL != null) {
      commandLine += "--production-public-base-url=${productionPublicBaseURL}"
    }
    if (this.environment.privateBaseUrl != null) {
      commandLine += "--override-private-base-url=${privateBaseUrl}"
    }
    toolbox.runToolbox(commandLine: commandLine,
                       jobName: "import",
                       openAPI: [
                         "filename": baseName,
                         "content": util.readFile(this.openapi.filename)
                       ])
  }

  void applyApplicationPlans() {
    def globalOptions = toolbox.getGlobalToolboxOptions()
    this.applicationPlans.each{
      def commandLine = [ "3scale", "application-plan", "apply" ] + globalOptions + [ this.toolbox.destination, this.environment.targetSystemName, it.systemName, "--approval-required=${it.approvalRequired}", "--cost-per-month=${it.costPerMonth}", "--end-user-required=${it.endUserRequired}", "--name=${it.name}", "--publish=${it.published}", "--setup-fee=${it.setupFee}", "--trial-period-days=${it.trialPeriodDays}" ]
      if (it.defaultPlan) {
        commandLine += "--default"
      }

      toolbox.runToolbox(commandLine: commandLine,
                         jobName: "apply-application-plan-${it.systemName}")
    }
  }

  void applyApplication(Map application) {
    assert application.name != null
    assert application.applicationPlan != null
    assert application.accountId != null

    def globalOptions = toolbox.getGlobalToolboxOptions()
    def commandLine = [ "3scale", "application", "apply" ] + globalOptions + this.toolbox.destination

    if (this.openapi.securityScheme == ThreescaleSecurityScheme.APIKEY 
     || this.openapi.securityScheme == ThreescaleSecurityScheme.OPEN) {
       
      commandLine += application.userKey
    } else if (this.openapi.securityScheme == ThreescaleSecurityScheme.OIDC) {
      commandLine += [ application.clientId}, "--application-key=${application.clientSecret}" ]
    } else {
      throw new Exception("NOT_IMPLEMENTED")
    }

    commandLine += [ "--name=${application.name}", 
                     "--description=${application.description != null ? application.description : "Created by the 3scale_toolbox from a Jenkins pipeline."}",
                     "--plan=${application.applicationPlan}",
                     "--service=${this.environment.targetSystemName}",
                     "--account=${application.accountId}" ]
    toolbox.runToolbox(commandLine: commandLine,
                       jobName: "apply-application")
  }

  Map readProxy(String environment) {
    def globalOptions = toolbox.getGlobalToolboxOptions()
    def commandLine = [ "3scale", "proxy-config", "show" ] + globalOptions + [ this.toolbox.destination, this.environment.targetSystemName, environment ]

    String proxyDefinition = toolbox.runToolbox(commandLine: commandLine,
                                                jobName: "show-proxy")

    Util util = new Util()
    return util.parseJson().content.proxy as Map
  }

  void promoteToProduction() {
    def globalOptions = toolbox.getGlobalToolboxOptions()
    def commandLine = "3scale proxy-config promote ${globalOptions} ${this.toolbox.destination} ${this.environment.targetSystemName}"
    toolbox.runToolbox(commandLine: commandLine,
                       jobName: "promote-to-production")

  }

  static String basename(path) {
      def filename = path.drop(path.lastIndexOf("/") != -1 ? path.lastIndexOf("/") + 1 : 0)
      filename = filename.replaceAll("[^-._a-zA-Z0-9]", "_")
      return filename
  }

}

