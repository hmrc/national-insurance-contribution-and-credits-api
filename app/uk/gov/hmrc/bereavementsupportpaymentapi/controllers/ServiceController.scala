package uk.gov.hmrc.bereavementsupportpaymentapi.controllers

import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.bereavementsupportpaymentapi.connectors.HipConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import uk.gov.hmrc.bereavementsupportpaymentapi.utils.Validator

@Singleton()
class ServiceController @Inject()(cc: ControllerComponents, connector: HipConnector, validators: Validator)
    extends BackendController(cc) {



  def getCitizenInfo: Action[AnyContent] = Action.async { implicit request =>
    // TODO add validation
    val queryParams: Map[String, Seq[String]] = request.queryString

    val flatQueryParams: Map[String, String] = queryParams.map {
      case (key, values) => key -> values.mkString(",")
    }

    val validatedNino = validators.ninoValidator(nino)
    }

    }

    println(flatQueryParams)
    // TODO call to backend

    Future.successful(Ok(connector.getCitizenInfo()))
  }
}
