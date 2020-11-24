#!groovy

package com.redhat

abstract class OpenAPI {
    String filename
    def content
    String version
    String majorVersion
    ThreescaleSecurityScheme securityScheme
    boolean validateOAS = true
    String descriptionWithTags

    OpenAPI(Map conf) {
        assert conf.filename != null
        assert conf.content != null
        this.filename = conf.filename
        this.content = conf.content
    }

    String getUpdatedContent() {
        Util util = new Util()
        String baseName = "updated-" + util.basename(this.filename)
        util.removeFile(baseName)
        util.writeOpenAPISpecificationFile(baseName, this.content)
        return util.readFile(baseName)
    }

    void updateTitleWithEnvironmentAndVersion(String environmentName) {
        String title = this.content.info.title as String
        String version = this.version as String
        String newTitle = null
        if (environmentName != null) {
            newTitle = "${title} (${environmentName.toUpperCase()}, v${version})"
        } else {
            newTitle = "${title} (v${version})"
        }
        this.content.info.title = newTitle
    }
    
    void getServiceTags() {    
        if (content.tags != null && content.tags.size() >= 1) {
            descriptionWithTags = this.content.info.description as String  
            descriptionWithTags += content.tags[0].name as String           
           } 
    }
          
    abstract void parseOpenAPISpecificationFile()
}
