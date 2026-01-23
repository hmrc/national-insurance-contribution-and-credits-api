package uk.gov.hmrc.app.benefitEligibility.common

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import scala.collection.immutable

sealed abstract class NpsErrorReason404(override val entryName: String) extends EnumEntry

object NpsErrorReason404 extends Enum[NpsErrorReason404] with PlayJsonEnum[NpsErrorReason404] {
  val values: immutable.IndexedSeq[NpsErrorReason404] = findValues

  case object NotFound extends NpsErrorReason404("Not Found")
}
