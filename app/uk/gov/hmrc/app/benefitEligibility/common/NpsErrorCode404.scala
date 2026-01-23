package uk.gov.hmrc.app.benefitEligibility.common

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import scala.collection.immutable

sealed abstract class NpsErrorCode404(override val entryName: String) extends EnumEntry

object NpsErrorCode404 extends Enum[NpsErrorCode404] with PlayJsonEnum[NpsErrorCode404] {
  val values: immutable.IndexedSeq[NpsErrorCode404] = findValues

  case object ErrorCode404 extends NpsErrorCode404("404")
}
