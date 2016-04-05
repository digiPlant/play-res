package se.digiplant.res

import org.specs2.matcher.MustThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.specification._
import org.specs2.mutable.Around
import org.specs2.execute.AsResult
import play.api.{Environment, Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import java.io.File
import org.apache.commons.io.FileUtils
import util.Random

trait ResContext extends Around with TempFile with Mockito with MustThrownExpectations {

  implicit val app: Application = new GuiceApplicationBuilder(
    configuration = Configuration("res.default" -> "tmp/default", "res.images" -> "tmp/images")
  ).build

  val res = new api.ResImpl(app)
  val resAssets = new ResAssets(mock[Environment], res)

  def around[T : AsResult](t: =>T) = Helpers.running(app) {
    val result = AsResult.effectively(t)

    tmp.delete()

    result
  }
}

trait TempFile extends Scope {
  val tmp = new File("tmp")
  val logo = new File("test/resources/digiPlant.jpg")

  def testFile: File = {
    tmp.mkdir()
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('1' to '9')
    val rand = (1 to 20).map(x => chars(Random.nextInt(chars.length))).mkString
    val tmpFile = new File("tmp", rand + ".jpg")
    FileUtils.copyFile(logo, tmpFile)
    tmpFile
  }

  def anonymousTestFile: File = {
    tmp.mkdir()
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('1' to '9')
    val rand = (1 to 20).map(x => chars(Random.nextInt(chars.length))).mkString
    val tmpFile = new File("tmp", rand)
    FileUtils.copyFile(logo, tmpFile)
    tmpFile
  }
}
