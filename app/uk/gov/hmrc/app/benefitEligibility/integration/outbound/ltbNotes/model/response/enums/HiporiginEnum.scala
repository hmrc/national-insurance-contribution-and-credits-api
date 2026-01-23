package uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.EnumAtcredfg.findValues

import scala.collection.immutable

sealed abstract class HiporiginEnum(override val entryName: String) extends EnumEntry

object HiporiginEnum extends Enum[HiporiginEnum] with PlayJsonEnum[HiporiginEnum] {
  val values: immutable.IndexedSeq[HiporiginEnum] = findValues

  case object Hip extends HiporiginEnum("HIP")
  case object Hod extends HiporiginEnum("HoD")
}
