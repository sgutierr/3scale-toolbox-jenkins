#!groovy

package com.redhat

class Product {

    String productfile
     
    Product (Map conf) {
        assert conf.productfile != null
        this.productfile = conf.productfile
    }

}

