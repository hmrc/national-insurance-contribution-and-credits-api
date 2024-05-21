package uk.gov.hmrc.bereavementsupportpaymentapi.controllers

import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.bereavementsupportpaymentapi.connectors.HipConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class ServiceController @Inject()(cc: ControllerComponents, connector: HipConnector)
    extends BackendController(cc) {

  def getNinoInfo(): Action[AnyContent] = Action.async { implicit request =>
    // TODO add validation
    val nino = request.getQueryString("nino").getOrElse(println("tes"))

    // TODO call to backend

    Future.successful(Ok(connector.getNinoInfo()))
  }
}
