package org.kreal.libftp.filesystem

import org.apache.ftpserver.ftplet.FileSystemView
import org.apache.ftpserver.ftplet.User

class FileSystemFactory : org.apache.ftpserver.ftplet.FileSystemFactory {

    override fun createFileSystemView(user: User?): FileSystemView = synchronized(user
            ?: throw IllegalArgumentException("user can not be null")) {
        org.kreal.libftp.filesystem.FileSystemView(user)
    }

}