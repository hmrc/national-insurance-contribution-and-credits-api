/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.app.benefitEligibility.service

import cats.data.EitherT
import cats.instances.future.*
import com.google.inject.Inject
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitEligibilityError, BenefitType}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.GYSPEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{EligibilityCheckDataResult, NpsApiResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultGYSP
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.connector.BenefitSchemeDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.connector.IndividualStatePensionInformationConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.connector.LongTermBenefitNotesConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.connector.MarriageDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.connector.NiContributionsAndCreditsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.connector.SchemeMembershipDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.util.ContributionCreditTaxWindowCalculator
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import cats.implicits.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.SchemeContractedOutNumberDetails
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsRequestHelper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.service.Test.benefitEligibilityErrorSemiGroup
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{
  BenefitSchemeDetailsResult,
  SchemeMembershipDetailsResult
}

case class Blah(
    schemeMembershipDetailsResult: SchemeMembershipDetailsResult,
    benefitSchemeDetailsResults: Option[List[BenefitSchemeDetailsResult]]
)

class GetYourStatePensionDataRetrievalService @Inject() (
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    benefitSchemeDetailsConnector: BenefitSchemeDetailsConnector,
    marriageDetailsConnector: MarriageDetailsConnector,
    longTermBenefitNotesConnector: LongTermBenefitNotesConnector,
    schemeMembershipDetailsConnector: SchemeMembershipDetailsConnector,
    statePensionInformationConnector: IndividualStatePensionInformationConnector,
    marriageDetailsRequestHelper: MarriageDetailsRequestHelper,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: GYSPEligibilityCheckDataRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultGYSP] = {

    val benefitType = eligibilityCheckDataRequest.benefitType

    val taxWindows = ContributionCreditTaxWindowCalculator.createTaxWindows(
      eligibilityCheckDataRequest.contributionsAndCredits.startTaxYear,
      eligibilityCheckDataRequest.contributionsAndCredits.endTaxYear
    )

    (
      taxWindows.map { window =>
        niContributionsAndCreditsConnector.fetchContributionsAndCredits(
          eligibilityCheckDataRequest.benefitType,
          NiContributionsAndCreditsRequest(
            eligibilityCheckDataRequest.nationalInsuranceNumber,
            eligibilityCheckDataRequest.contributionsAndCredits.dateOfBirth,
            window.startTaxYear,
            window.endTaxYear
          )
        )
      }.sequence,
      marriageDetailsConnector.fetchMarriageDetails(
        benefitType,
        marriageDetailsRequestHelper.buildRequestPath(appConfig.hipBaseUrl, eligibilityCheckDataRequest)
      ),
      getSchemeMembershipAndDetails(eligibilityCheckDataRequest),
      longTermBenefitNotesConnector.fetchLongTermBenefitNotes(
        benefitType,
        eligibilityCheckDataRequest.nationalInsuranceNumber,
        eligibilityCheckDataRequest.benefitCalculationNotes.benefitType,
        eligibilityCheckDataRequest.benefitCalculationNotes.associatedCalculationSequenceNumber
      ),
      statePensionInformationConnector.fetchIndividualStatePensionInformation(
        benefitType,
        eligibilityCheckDataRequest.nationalInsuranceNumber
      )
    ).parTupled.map {
      case (
            contributionsAndCreditResult,
            marriageDetailsResult,
            blah,
            longTermBenefitNotesResult,
            individualStatePensionResult
          ) =>
        EligibilityCheckDataResultGYSP(
          contributionsAndCreditResult,
          blah.schemeMembershipDetailsResult,
          blah.benefitSchemeDetailsResults match {
            case Some(value) => value
            case None        => Nil
          },
          marriageDetailsResult,
          longTermBenefitNotesResult,
          individualStatePensionResult
        )
    }

  }

  private[service] def getSchemeMembershipAndDetails(
      eligibilityCheckDataRequest: GYSPEligibilityCheckDataRequest
  )(implicit headerCarrier: HeaderCarrier): EitherT[
    Future,
    BenefitEligibilityError,
    Blah
  ] = for {
    detailsResult <- schemeMembershipDetailsConnector.fetchSchemeMembershipDetails(
      benefitType = eligibilityCheckDataRequest.benefitType,
      nationalInsuranceNumber = eligibilityCheckDataRequest.nationalInsuranceNumber,
      sequenceNumber = eligibilityCheckDataRequest.schemeMembershipDetails.schemeMembershipSequenceNumber,
      transferSequenceNumber =
        eligibilityCheckDataRequest.schemeMembershipDetails.schemeMembershipTransferSequenceNumber,
      occurrenceNumber = eligibilityCheckDataRequest.schemeMembershipDetails.schemeMembershipOccurrenceNumber
    )
    resultTuple <- {
      detailsResult.getSuccess match {
        case Some(successResponse) =>
          successResponse.schemeMembershipDetailsSummaryList
            .map(_.flatMap(_.schemeMembershipDetails.employersContractedOutNumberDetails)) match {
            case Some(contractedOutNumberDetailsList) =>
              contractedOutNumberDetailsList
                .map { contractedOutNumberDetails =>
                  benefitSchemeDetailsConnector.fetchBenefitSchemeDetails(
                    eligibilityCheckDataRequest.benefitType,
                    eligibilityCheckDataRequest.nationalInsuranceNumber,
                    SchemeContractedOutNumberDetails(contractedOutNumberDetails.value)
                  )
                }
                .sequence
                .flatMap(i => EitherT.pure[Future, BenefitEligibilityError]((detailsResult, Some(i))))
            case None => EitherT.pure[Future, BenefitEligibilityError]((detailsResult, None))
          }
        case None => EitherT.pure[Future, BenefitEligibilityError]((detailsResult, None))
      }
    }

  } yield Blah(resultTuple._1, resultTuple._2)

}
