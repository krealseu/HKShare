package org.kreal.libftp

import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser

class K {
    fun ss() {
        val serverFactory = FtpServerFactory()
        //服务用户管理
        val mUserManager = PropertiesUserManagerFactory().createUserManager()
        serverFactory.userManager = mUserManager
        val baseUser = BaseUser()
        baseUser.name = "anonymous"
        baseUser.homeDirectory = "/sdcard"
        mUserManager.save(baseUser)
        //自定义文件系统
//        serverFactory.fileSystem = FileSystemFactoryCC(context)

        //服务监听端口
        val listenerFactory = ListenerFactory()
        listenerFactory.port = 2121
        serverFactory.addListener("default", listenerFactory.createListener())
        serverFactory.createServer().start()
    }
}