package se.digiplant.res.api

import javax.inject.{Singleton, Inject}

import play.api._
import libs.{Files, MimeTypes}
import java.io.{FileInputStream, File}
import org.apache.commons.io.{FilenameUtils, FileUtils}
import org.apache.commons.codec.digest.DigestUtils

trait Res {

  /**
   * Retrieves a file with the specified fileuid and if specified all meta attributes
   * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return Option[File]
   */
  def get(fileuid: String, source: String = "default", meta: Seq[String] = Nil): Option[File]

  /**
   * Puts a file into the supplied source
   * @param file A file to be stored
   * @param source The configured source name
   * @param filename Override the sha1 checksum generated filename
   * @param extension The extension of the file
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @throws java.lang.IllegalArgumentException
   * @return The unique file name with the metadata appended
   */
  @throws(classOf[IllegalArgumentException])
  def put(file: File, source: String = "default", filename: Option[String] = None, extension: Option[String] = None, meta: Seq[String] = Nil): String

  /**
   * Puts a filePart into the supplied source and tries to figure out it's correct mimetype and extension (Java version)
   * @param filePart A filePart that's been uploaded to play to be stored
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @throws java.lang.IllegalArgumentException
   * @return The unique file name with the metadata appended
   */
  @throws(classOf[IllegalArgumentException])
  def put(filePart: play.mvc.Http.MultipartFormData.FilePart[File], source: String, meta: Seq[String]): String

  /**
   * Puts a filePart into the supplied source and tries to figure out it's correct mimetype and extension
   * @param filePart A filePart that's been uploaded to play to be stored
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @throws java.lang.IllegalArgumentException
   * @return The unique file name with the metadata appended
   */
  @throws(classOf[IllegalArgumentException])
  def put(filePart: play.api.mvc.MultipartFormData.FilePart[Files.TemporaryFile], source: String, meta: Seq[String]): String

  /**
   * Deletes a file with the specified fileuid
   * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return true if file was deleted, false if it failed
   */
  def delete(fileuid: String, source: String = "default", meta: Seq[String] = Nil): Boolean

  /**
   * Retrieves a file with the specified filepath and if specified all meta attributes
   * @param filePath The filepath relative to the play app, it can also include the meta if you don't want to specify it separately
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return Option[File]
   */
  def fileWithMeta(filePath: String, meta: Seq[String] = Nil): Option[File]

  /**
   * Puts a file into the supplied source
   * @param file A file to be stored
   * @param filePath The filepath relative to the play app
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique filePath with the metadata appended
   */
  def saveWithMeta(file: File, filePath: String, meta: Seq[String] = Nil): String
}

@Singleton
class ResImpl @Inject()(environment: Environment, configuration: Configuration) extends Res {

  lazy val config = configuration.getConfig("res").getOrElse(Configuration.empty)

  lazy val sources: Map[String, File] = config.subKeys.map {
    sourceKey =>
      val path = config.getString(sourceKey).getOrElse(throw config.reportError("res." + sourceKey, "Missing res path[" + sourceKey + "]"))
      val file = new File(FilenameUtils.concat(environment.rootPath.getAbsolutePath, path))
      if (file.isDirectory && !file.exists()) {
        FileUtils.forceMkdir(file)
      }
      sourceKey -> file
  }.toMap

  /**
   * Retrieves a file with the specified fileuid and if specified all meta attributes
   * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return Option[File]
   */
  def get(fileuid: String, source: String = "default", meta: Seq[String] = Nil): Option[File] = {
    sources.get(source).flatMap { dir =>
      val filename = if (meta.isEmpty)
          fileuid
        else {
          val name = FilenameUtils.getBaseName(fileuid)
          val ext = FilenameUtils.getExtension(fileuid)
          name + meta.mkString(if (meta.nonEmpty) { "_" } else "", "_", ".") + ext
        }
      Option(FileUtils.getFile(dir, hashAsDirectories(filename), filename)).filter(_.exists)
    }
  }

  /**
   * Puts a file into the supplied source
   * @param file A file to be stored
   * @param source The configured source name
   * @param filename Override the sha1 checksum generated filename
   * @param extension The extension of the file
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique file name with the metadata appended
   */
  @throws(classOf[IllegalArgumentException])
  def put(file: File, source: String = "default", filename: Option[String] = None, extension: Option[String] = None, meta: Seq[String] = Nil): String = {
    val extensionOptions = List(
      extension,
      filename.flatMap(x => Option(FilenameUtils.getExtension(x))),
      Option(FilenameUtils.getExtension(file.getName))
    )
    val ext = extensionOptions.collectFirst { case Some(x) => x }

    require(ext.isDefined, "file must have extension or extension must be specified ["+ file.getName +"]")
    require(sources.get(source).isDefined, "Source: " + source + " doesn't exist, make sure you have specified it in conf/application.conf.")

    val fis = new FileInputStream(file)
    val name = filename.map(FilenameUtils.getBaseName).getOrElse(DigestUtils.sha1Hex(fis))
    fis.close()
    
    require(name.length > 12, "name must contain atleast 12 chars to be able to be stored properly.")

    val dir = sources.get(source).get

    val base = new File(dir, hashAsDirectories(name))
    if (!base.exists()) {
      base.mkdirs()
    }

    val fileuid = name + meta.mkString(if (meta.nonEmpty) "_" else "", "_", ".") + ext.get
    val target = new File(base, fileuid)

    if (!target.exists()) {
      FileUtils.moveFile(file, target)
    }
    fileuid
  }

  /**
   * Puts a filePart into the supplied source and tries to figure out it's correct mimetype and extension (Java version)
   * @param filePart A filePart that's been uploaded to play to be stored
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique file name with the metadata appended
   */
  @throws(classOf[IllegalArgumentException])
  def put(filePart: play.mvc.Http.MultipartFormData.FilePart[File], source: String, meta: Seq[String]): String = {
    val extensionOptions = List(
      Option(FilenameUtils.getExtension(filePart.getFilename)),
      getExtensionFromMimeType(Option(filePart.getContentType))
    )
    val ext = extensionOptions.collectFirst { case Some(x) => x }
    put(filePart.getFile, source, None, ext, meta)
  }

  /**
   * Puts a filePart into the supplied source and tries to figure out it's correct mimetype and extension
   * @param filePart A filePart that's been uploaded to play to be stored
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique file name with the metadata appended
   */
  @throws(classOf[IllegalArgumentException])
  def put(filePart: play.api.mvc.MultipartFormData.FilePart[Files.TemporaryFile], source: String, meta: Seq[String]): String = {
    val extensionOptions = List(
      Option(FilenameUtils.getExtension(filePart.filename)),
      getExtensionFromMimeType(filePart.contentType)
    )
    val ext = extensionOptions.collectFirst { case Some(x) => x }
    put(filePart.ref.file, source, None, ext, meta)
  }

  /**
   * Deletes a file with the specified fileuid
   * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return true if file was deleted, false if it failed
   */
  def delete(fileuid: String, source: String = "default", meta: Seq[String] = Nil): Boolean = get(fileuid, source, meta).exists(_.delete())

  private def hashAsDirectories(hash: String): String = hash.substring(0, 4) + File.separator + hash.substring(4, 8)

  /**
   * Retrieves a file with the specified filepath and if specified all meta attributes
   * @param filePath The filepath relative to the play app, it can also include the meta if you don't want to specify it separately
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return Option[File]
   */
  def fileWithMeta(filePath: String, meta: Seq[String] = Nil): Option[File] = {
    val path = FilenameUtils.getPath(filePath)
    val name = FilenameUtils.getBaseName(filePath)
    val ext = FilenameUtils.getExtension(filePath)
    environment.getExistingFile(path + name + meta.mkString(if (meta.nonEmpty) { "_" } else "", "_", ".") + ext)
  }

  /**
   * Puts a file into the supplied source
   * @param file A file to be stored
   * @param filePath The filepath relative to the play app
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique filePath with the metadata appended
   */
  def saveWithMeta(file: File, filePath: String, meta: Seq[String] = Nil): String = {

    val path = FilenameUtils.getPath(filePath)
    val name = FilenameUtils.getBaseName(filePath)
    val ext = FilenameUtils.getExtension(filePath)

    val base = environment.getFile(path)
    if (!base.exists()) {
      base.mkdirs()
    }

    val targetPath = path + name + meta.mkString(if (meta.nonEmpty) { "_" } else "", "_", ".") + ext
    val target = environment.getFile(targetPath)
    if (target.exists()) {
      FileUtils.copyFile(file, target)
    } else {
      FileUtils.moveFile(file, target)
    }

    targetPath
  }

  /**
   * Gets the file extension of known file types, but because the MimeTypes map isn't sorted properly we need to override the images for play-scalr
   * otherwise it chooses the top most extension for image/jpeg = jfif
   * @param mime The mimetype
   * @return a file extension without the .
   */
  private def getExtensionFromMimeType(mime: Option[String]): Option[String] = mime match {
    case None => None
    case Some("image/jpeg") => Some("jpg")
    case Some("image/png") => Some("png")
    case Some("image/gif") => Some("gif")
    case Some(m) => MimeTypes.types.map(_.swap).get(m)
  }
}
