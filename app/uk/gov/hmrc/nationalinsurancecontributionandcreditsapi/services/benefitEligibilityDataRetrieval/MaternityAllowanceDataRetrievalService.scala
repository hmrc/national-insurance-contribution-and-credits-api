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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.services.benefitEligibilityDataRetrieval

import cats.data.EitherT
import cats.instances.future._
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.api.EligibilityCheckDataRequest
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain.EligibilityCheckDataResult

import scala.concurrent.{ExecutionContext, Future}

class MaternityAllowanceDataRetrievalService(implicit ec: ExecutionContext)
    extends BenefitEligibilityDataRetrievalService {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: EligibilityCheckDataRequest
  ): EitherT[Future, Throwable, EligibilityCheckDataResult] =
    EitherT.pure[Future, Throwable](EligibilityCheckDataResult("MaternityAllowanceDataRetrievalService Result"))

}
