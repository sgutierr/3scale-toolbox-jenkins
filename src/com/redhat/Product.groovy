#!groovy

package com.redhat

class Product {

    String productFile
     
    Product (Map conf) {
        assert conf.productfile != null
        this.productFile = conf.productfile
    }

}

