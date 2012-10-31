package se.digiplant.res

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._

object ResAssetsSpec extends Specification {

  val ctx = new ResContext()

  "ResAssets Controller" should {

    "return resource" in {
      running(ctx.app) {
        api.Res.put(ctx.testFile)

        val result = ResAssets.at("5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg")(FakeRequest())

        status(result) must equalTo(OK)
        contentType(result) must beSome("image/jpeg")
      }
    }

    "return resource in supplied source" in {
      running(ctx.app) {
        api.Res.put(ctx.testFile, "images")

        val result = ResAssets.at("5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg", "images")(FakeRequest())

        status(result) must equalTo(OK)
        contentType(result) must beSome("image/jpeg")
      }
    }
  }
}
