package org.kreal.libftp.filesystem

import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile
import org.apache.ftpserver.ftplet.FtpFile
import org.apache.ftpserver.ftplet.User

class FileSystemView(private val user: User) : org.apache.ftpserver.ftplet.FileSystemView {
    private lateinit var rootDir: String
    private lateinit var currDir: String
    private var caseInsensitive: Boolean = false

    init {
        var rootDir = user.homeDirectory
        if (!rootDir.endsWith("/")) {
            rootDir = "$rootDir/"
        }
        this.rootDir = rootDir
        this.currDir = "/"
    }

    override fun getHomeDirectory(): FtpFile {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getWorkingDirectory(): FtpFile {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFile(p0: String?): FtpFile {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeWorkingDirectory(p0: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isRandomAccessible(): Boolean = true

    override fun dispose() {
    }

}