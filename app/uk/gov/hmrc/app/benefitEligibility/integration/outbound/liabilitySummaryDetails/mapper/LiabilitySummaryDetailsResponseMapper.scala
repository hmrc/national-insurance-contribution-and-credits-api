package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.mapper

import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Liabilities
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{AccessForbidden, BadRequest, UnprocessableEntity}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{ApiResult, LiabilityResult, NpsResponseMapper}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsError.{
  LiabilitySummaryDetailsErrorResponse400,
  LiabilitySummaryDetailsErrorResponse403,
  LiabilitySummaryDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse

class LiabilitySummaryDetailsResponseMapper extends NpsResponseMapper[LiabilitySummaryDetailsResponse, ApiResult] {

  def toApiResult(response: LiabilitySummaryDetailsResponse): LiabilityResult =
    response match {
      case LiabilitySummaryDetailsErrorResponse400(failures) =>
        DownstreamErrorReport(Liabilities, BadRequest)
      case LiabilitySummaryDetailsErrorResponse403(reason, code) =>
        DownstreamErrorReport(Liabilities, AccessForbidden)
      case LiabilitySummaryDetailsErrorResponse422(failures, askUser, fixRequired, workItemRaised) =>
        DownstreamErrorReport(Liabilities, UnprocessableEntity)
      case response: LiabilitySummaryDetailsSuccessResponse => DownstreamSuccessResponse(Liabilities, response)
    }

}
