package se.digiplant.resource.api

import org.specs2.mutable.Specification
import play.api._
import play.api.test._
import play.api.test.Helpers._

object ResourcesSpec extends Specification {

  implicit val ctx = new ResContext()

  "Resources Controller" should {

    "return resource" in {
      ctx.res.put(ctx.getTestFile)

      val result = Resources.at("5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg")(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("image/jpeg")
    }

    "return resource in supplied source" in {
      ctx.res.put(ctx.getTestFile, "images")

      val result = Resources.at("5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg", "images")(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("image/jpeg")
    }

  }
}
