#!groovy

package com.redhat

class Product {

    String productfile
     
    Product (Map conf) {
        this.productfile = conf.productfile
    }

}

