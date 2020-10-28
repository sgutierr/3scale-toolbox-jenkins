#!groovy

package com.redhat

class OpenAPI3 extends OpenAPI {
    OpenAPI3(Map conf) {
        super(conf);
    }

    void parseOpenAPISpecificationFile() {
        assert content.openapi != null
        assert content.openapi.startsWith("3.0")
        this.version = content.info.version
        assert this.version != null
        this.majorVersion = version.tokenize(".")[0]

        String securitySchemeName = null
        def securityDefinitions = content.components?.securitySchemes
        if (content.security != null && content.security.size() == 1) {
            Set securitySchemes = content.security[0].keySet()
            if (securitySchemes.size() == 1) {
                securitySchemeName = securitySchemes.first()
            } else {
                throw new Exception("Cannot handle OpenAPI Specifications with multiple security requirements or no global requirement. Found ${content.security.size()}/${securitySchemes.size()} security requirements.")
            }

        } else if ((content.security == null || content.security.size() == 0)
                && (securityDefinitions == null || securityDefinitions.size() == 0)) {

            // To make it explicit that this is an Open API, we require no security requirement
            // and no security definition (better be safe than sorry...)
            this.securityScheme = ThreescaleSecurityScheme.OPEN
        } else {
            throw new Exception("Cannot handle OpenAPI Specifications with multiple security requirements or no global requirement. Found ${content.security != null ? content.security.size() : "no"} security requirements.")
        }

        if (securitySchemeName != null) {
            if (securityDefinitions != null
             && securityDefinitions.get(securitySchemeName) != null) {

                Map securityScheme = securityDefinitions.get(securitySchemeName)
                String securityType = securityScheme.type

                if (securityType == "oauth2") {
                    this.securityScheme = ThreescaleSecurityScheme.OIDC
                } else if (securityType == "apiKey") {
                    this.securityScheme = ThreescaleSecurityScheme.APIKEY
                } else {
                    throw new Exception("Cannot handle OpenAPI Specifications with security scheme: ${securityType}")
                }
            } else {
                throw new Exception("Cannot find security scheme ${securitySchemeName} in OpenAPI Specifications")
            }
        } // else: this is an Open API
    }
}
