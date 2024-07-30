package org.oorexx.doc.generate.v2

import java.io.IOException
import java.net.URI
import java.net.URL
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

object JarUtil {
    @JvmStatic
    fun getPath(source: String): Path {
        return try {
            val resource = JarUtil::class.java.classLoader.getResource(source)!!.toURI()
            FileSystems.newFileSystem(resource, emptyMap<String, String>()).use { fileSystem ->
                fileSystem.getPath(source)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun copyFromJarToTmp(source: String): Path {
        val tempDirectory: Path = try {
            Files.createTempDirectory(null as String?)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        // Register the temp directory for deletion on JVM exit
        tempDirectory.toFile().deleteOnExit()
        Runtime.getRuntime().addShutdownHook(Thread {
            deleteDirectoryRecursively(tempDirectory)
        })

        copyFromJar(source, tempDirectory)
        return tempDirectory
    }

    @JvmStatic
    fun copyFromJar(source: String, target: Path) {
        try {
            val resource = JarUtil::class.java.classLoader.getResource(source)!!.toURI()
            FileSystems.newFileSystem(resource, emptyMap<String, String>()).use { fileSystem ->
                val jarPath = fileSystem.getPath(source)

                Files.walkFileTree(jarPath, object : SimpleFileVisitor<Path>() {
                    private lateinit var currentTarget: Path

                    @Throws(IOException::class)
                    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                        currentTarget = target.resolve(jarPath.relativize(dir).toString())
                        Files.createDirectories(currentTarget)
                        return FileVisitResult.CONTINUE
                    }

                    @Throws(IOException::class)
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        Files.copy(file, target.resolve(jarPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING)
                        return FileVisitResult.CONTINUE
                    }
                })
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun isJar(): Boolean {
        val classLocation: URL? = JarUtil::class.java.protectionDomain.codeSource.location
        classLocation ?: return false
        val path = classLocation.toString()
        return path.endsWith(".jar")
    }

    private fun deleteDirectoryRecursively(directory: Path) {
        Files.walkFileTree(directory, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                Files.delete(dir)
                return FileVisitResult.CONTINUE
            }
        })
    }
}
