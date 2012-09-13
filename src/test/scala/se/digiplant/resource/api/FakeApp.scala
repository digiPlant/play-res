package se.digiplant.resource.api

import org.specs2.specification.{Scope, Around}
import play.api._
import play.api.test._
import play.api.test.Helpers._
import java.io.File
import org.apache.commons.io.FileUtils

trait FakeApp extends Around with Scope with FileSystemScope {

  val plugins = Seq(
    "se.digiplant.resource.api.ResourcePlugin"
  )

  val configuration = Map(
    ("res.default" -> "tmp/default"),
    ("res.images" -> "tmp/images")
  )

  lazy val res = app.plugin[ResourcePlugin].get

  object app extends FakeApplication(
    additionalPlugins = plugins,
    additionalConfiguration = configuration
  )

  def around[T <% org.specs2.execute.Result](test: => T) = running(app) {
    test
  }
}

trait FileSystemScope extends Scope {
  val tmp = new File("tmp")
  val logo = new File("src/test/resources/digiPlant.jpg")
  var testFile = new File("tmp/digiPlant.jpg")

  def getTestFile(): File = {
    tmp.mkdir()
    FileUtils.copyFile(logo, testFile)
    testFile
  }
}

object Files extends FileSystemScope