package se.digiplant.res

import se.digiplant.res.api.ResPlugin
import org.specs2.specification.{BeforeAfterAround, Scope}
import org.specs2.execute.Result
import play.api._
import play.api.test._
import play.api.test.Helpers._
import java.io.File
import org.apache.commons.io.FileUtils
import util.Random

class ResContext(val app: FakeApplication = new FakeApplication(
  additionalPlugins = Seq(
    "se.digiplant.res.api.ResPlugin"
  ),
  additionalConfiguration = Map(
    ("res.default" -> "tmp/default"),
    ("res.images" -> "tmp/images")
  )
)) extends BeforeAfterAround with TempFile {

  implicit val implicitApp = app

  lazy val res = app.plugin[ResPlugin].get

  def around[T](t: => T)(implicit evidence: (T) => Result) = running(app)(t)

  def before {
  }

  def after {
    tmp.delete()
  }
}

trait TempFile extends Scope {
  val tmp = new File("tmp")
  val logo = new File("test/resources/digiPlant.jpg")

  def getTestFile(): File = {
    tmp.mkdir()
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('1' to '9')
    val rand = (1 to 20).map(x => chars(Random.nextInt(chars.length))).mkString
    val tmpFile = new File("tmp", rand + ".jpg")
    FileUtils.copyFile(logo, tmpFile)
    tmpFile
  }

  def getAnonymousTestFile(): File = {
    tmp.mkdir()
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('1' to '9')
    val rand = (1 to 20).map(x => chars(Random.nextInt(chars.length))).mkString
    val tmpFile = new File("tmp", rand)
    FileUtils.copyFile(logo, tmpFile)
    tmpFile
  }
}
