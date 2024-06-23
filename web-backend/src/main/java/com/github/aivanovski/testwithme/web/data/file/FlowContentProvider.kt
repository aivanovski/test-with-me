package com.github.aivanovski.testwithme.web.data.file

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.entity.exception.AppException
import com.github.aivanovski.testwithme.web.entity.exception.AppIoException
import com.github.aivanovski.testwithme.web.entity.exception.FileNotFoundException
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class FlowContentProvider {

    fun getContent(path: String): Either<AppException, String> = either {
        val cacheDir = getCacheDir().bind()

        val file = File(cacheDir, path)
        if (!file.exists()) {
            raise(FileNotFoundException(path))
        }

        try {
            val bytes = FileInputStream(file).readAllBytes()
            String(bytes)
        } catch (exception: IOException) {
            raise(AppIoException(exception))
        }
    }

    private fun getCacheDir(): Either<AppException, File> = either {
        val currentDir = System.getProperty("user.dir")
        if (currentDir.isNullOrBlank()) {
            raise(AppException("Unable to get current dir"))
        }

        val file = File("$currentDir/flows", "test")
        val parent = file.parentFile
        if (!parent.exists() && !parent.mkdirs()) {
            raise(AppException("Unable to create directory: ${parent.path}"))
        }

        parent
    }
}