package org.kreal.hkshare.extensions

import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

/**
 * Created by lthee on 2017/11/25.
 */

val ip: String
    get() {
        try {
            for (enNetI in NetworkInterface
                    .getNetworkInterfaces()) {
                for (enumIpAddress in enNetI.inetAddresses) {
                    if (enumIpAddress is Inet4Address && !enumIpAddress.isLoopbackAddress()) {
                        if (enumIpAddress.getHostAddress().startsWith("192"))
                            return enumIpAddress.getHostAddress()
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return "localhost"
    }

fun ip(): String {
    try {
        for (enNetI in NetworkInterface
                .getNetworkInterfaces()) {
            for (enumIpAddress in enNetI.inetAddresses) {
                if (enumIpAddress is Inet4Address && !enumIpAddress.isLoopbackAddress()) {
                    return enumIpAddress.getHostAddress()
                }
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
    }
    return ""
}