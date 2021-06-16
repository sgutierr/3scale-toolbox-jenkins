# Changes made respect the master branch 
## Add tags in 3scale using the 3scale description field

Iterate for all tags in the OpenAPI specification and append them to the description field: openapi.groovy
```
    void getServiceTags() {    
        if (content.tags != null && content.tags.size() >= 1) {
            descriptionWithTags = "TAGS:"
            content.tags.each {
                descriptionWithTags += " ${it.name}"           
            }  
            descriptionWithTags += ". ${this.content.info.description}"          
        }    
```

Create a new function for running toolbox to update the description field into the service: threescaleService.groovy

``` 
   void applyDescriptioWithTags() {
        def globalOptions = toolbox.getGlobalToolboxOptions()
        def commandLine
        this.openapi.getServiceTags() 
        if (this.openapi.descriptionWithTags != null) {          
           commandLine = ["3scale", "service", "apply"] + globalOptions + [this.toolbox.destination, this.environment.targetSystemName]
           commandLine += ["--description=${this.openapi.descriptionWithTags}"]
           toolbox.runToolbox(commandLine: commandLine,
                    jobName: "add-description-tags")
       } 
``` 
