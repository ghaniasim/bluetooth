package com.example.bluetooth

class Device (var name: String,
              var address: String,
              var strength: Int? = 0) {

    override fun toString() : String {
        return "$name    $address    $strength dBm"
    }
}