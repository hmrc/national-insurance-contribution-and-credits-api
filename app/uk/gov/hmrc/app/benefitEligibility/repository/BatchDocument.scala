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

package uk.gov.hmrc.app.benefitEligibility.repository

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainText}

import java.time.Instant
import scala.util.Try

final case class BatchDocument(
    correlationId: CorrelationId,
    batchId: BatchId,
    data: JsObject,
    createdAt: Instant
) {

  def encrypt(encrypterDecrypter: Encrypter & Decrypter): BatchDocument = {
    val batch         = PlainText(Json.stringify(data))
    val encryptedData = encrypterDecrypter.encrypt(batch).value
    this.copy(
      data = Json.obj("encrypted" -> encryptedData)
    )
  }

  def decrypt(encrypterDecrypter: Encrypter & Decrypter): Option[BatchDocument] =
    Try {
      val dataAsString    = (data \ "encrypted").as[String]
      val decryptedString = encrypterDecrypter.decrypt(Crypted(dataAsString)).value
      this.copy(data = Json.parse(decryptedString).as[JsObject])
    }.toOption

}

object BatchDocument {
  implicit val batchDocumentFormat: Format[BatchDocument] = Json.format[BatchDocument]
}
