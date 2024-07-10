package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors

import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.controllers.actions.AuthAction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeAuthAction @Inject()(override val parser: BodyParsers.Default, override val authConnector: AuthConnector)
                              (implicit override val executionContext: ExecutionContext) extends AuthAction(authConnector, parser) {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    block(request)
}
