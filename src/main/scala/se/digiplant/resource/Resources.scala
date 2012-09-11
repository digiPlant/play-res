package se.digiplant.resource

import play.api.{PlayException, Application}
import play.api.mvc._

class Resources {
  def at(source: String, file: String)(implicit app: Application): Action[AnyContent] = {
    val plugin = app.plugin(classOf[ResourcePlugin]).getOrElse(throw PlayException("ResourcePlugin is not registered.", "You need to register the plugin with \"2000:se.digiplant.resource.ResourcePlugin\" in conf/play.plugins"))
    val path = if (plugin.resourceDirectories.containsKey(source))
      plugin.resourceDirectories.get(source).getAbsolutePath
    else
      throw PlayException("res", "source specified isn't configured in conf/application.conf")

    controllers.Assets.at(path, file)
  }
}
