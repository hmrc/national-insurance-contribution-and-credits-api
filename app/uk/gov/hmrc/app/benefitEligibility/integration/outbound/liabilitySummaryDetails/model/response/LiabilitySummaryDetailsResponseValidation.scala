/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response

import cats.data.{Validated, ValidatedNel}
import cats.implicits.{catsSyntaxNestedFoldable, toFunctorOps}
import uk.gov.hmrc.app.benefitEligibility.common.Identifier
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsError.{
  LiabilitySummaryDetailsErrorResponse400,
  LiabilitySummaryDetailsErrorResponse403,
  LiabilitySummaryDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.{
  LiabilityDetailsList,
  LiabilitySummaryDetailsSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.util.{NpsResponseValidator, SuccessfulResult}
import uk.gov.hmrc.app.benefitEligibility.util.validation.{
  MoneyValidation,
  NumberValidation,
  StringLengthValidation,
  StringPatternValidation
}

object LiabilitySummaryDetailsResponseValidation {

  // region Success Validation

  implicit val liabilitySummaryDetailsResponseValidator
      : NpsResponseValidator[LiabilitySummaryDetailsSuccessResponse] = {
    (response: LiabilitySummaryDetailsSuccessResponse) =>
      if (response.liabilityDetailsList.getOrElse(false).isInstanceOf[LiabilityDetailsList]) {
        liabilityDetailsValidate(response.liabilityDetailsList.get).sequence_.as(SuccessfulResult)
      } else {
        response.liabilityEmploymentDetailsList.get
          .flatMap { lE =>
            liabilityDetailsValidate(lE.liabilityDetails)
            ++
            List(
              StringPatternValidation.validate(lE.employmentStatusForLiability, "^(EMP|UNEMP| )$".r),
              StringLengthValidation.validate(lE.employmentStatusForLiability, 1, 5)
            )
          }
          .sequence_
          .as(SuccessfulResult)
      }
  }

  private def liabilityDetailsValidate(liabilityDetails: List[LiabilityDetailsList]) =
    liabilityDetails.flatMap { lD =>
      List(
        lD.identifier.map(s => StringPatternValidation.validate(s, Identifier.pattern)),
        lD.occurrenceNumber.map(n => NumberValidation.validate(n, 1, 32766)),
        lD.casepaperReferenceNumber.map(s => StringPatternValidation.validate(s, "^[A-Z0-9/ -]+$".r)),
        lD.casepaperReferenceNumber.map(s => StringLengthValidation.validate(s, 1, 9)),
        lD.homeResponsibilitiesProtectionBenefitReference.map(s =>
          StringPatternValidation.validate(s, "^[A-Za-z0-9- ]+$".r)
        ),
        lD.homeResponsibilitiesProtectionBenefitReference.map(s => StringLengthValidation.validate(s, 1, 10)),
        lD.homeResponsibilitiesProtectionRate.map(MoneyValidation.validateSigned),
        lD.homeResponsibilityProtectionCalculationYear.map(n => NumberValidation.validate(n, 1975, 2099)),
        lD.awardAmount.map(MoneyValidation.validateSigned),
        lD.resourceGroupIdentifier.map(n => NumberValidation.validate(n, 0, 99999999)),
        lD.officeDetails.flatMap(oF => oF.officeLocationDecode.map(n => NumberValidation.validate(n, 0, 99999))),
        lD.officeDetails.flatMap(oF => oF.officeLocationValue.map(s => StringLengthValidation.validate(s, 1, 128)))
      ).flatten
    }

  // endregion Success Validation

  // region Error Validation

  implicit val liabilitySummaryDetailsError422ResponseValidator
      : NpsResponseValidator[LiabilitySummaryDetailsErrorResponse422] =
    (t: LiabilitySummaryDetailsErrorResponse422) =>
      t.failures
        .flatMap { f =>
          List(
            StringLengthValidation.validate(f.reason, minLength = 1, maxLength = 128),
            StringLengthValidation.validate(f.code, minLength = 1, maxLength = 10)
          )
        }
        .sequence_
        .as(SuccessfulResult)

  implicit val liabilitySummaryDetailsErrorResponse400Validator
      : NpsResponseValidator[LiabilitySummaryDetailsErrorResponse400] =
    (t: LiabilitySummaryDetailsErrorResponse400) =>
      t.failures
        .map(f => StringLengthValidation.validate(f.reason, minLength = 1, maxLength = 128))
        .sequence_
        .as(SuccessfulResult)

  implicit val liabilitySummaryDetailsErrorResponse403Validator
      : NpsResponseValidator[LiabilitySummaryDetailsErrorResponse403] = {
    (t: LiabilitySummaryDetailsErrorResponse403) => Validated.validNel(SuccessfulResult)
  }

  // endregion Error Validation

}
