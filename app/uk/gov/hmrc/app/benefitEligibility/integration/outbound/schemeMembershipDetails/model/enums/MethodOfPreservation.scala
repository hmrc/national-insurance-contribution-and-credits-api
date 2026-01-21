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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class MethodOfPreservation(override val entryName: String) extends EnumEntry

object MethodOfPreservation extends Enum[MethodOfPreservation] with PlayJsonEnum[MethodOfPreservation] {
  val values: immutable.IndexedSeq[MethodOfPreservation] = findValues

  case object NotApplicable0                   extends MethodOfPreservation("NOT APPLICABLE (0)")
  case object ContributionsEquivalentPremium1  extends MethodOfPreservation("CONTRIBUTIONS EQUIVALENT PREMIUM (1)")
  case object GuaranteedMinimumPension2        extends MethodOfPreservation("GUARANTEED MINIMUM PENSION (2)")
  case object TransferFromCosrToCosr3          extends MethodOfPreservation("TRANSFER FROM COSR TO COSR (3)")
  case object TransferFromCosrToComp4          extends MethodOfPreservation("TRANSFER FROM COSR TO COMP (4)")
  case object TransferFromCosrToPp5            extends MethodOfPreservation("TRANSFER FROM COSR TO PP (5)")
  case object ChangeOfRpaFromCosrToCosr6       extends MethodOfPreservation("CHANGE OF RPA FROM COSR TO COSR (6)")
  case object ChangeOfRpaFromCosrToComp7       extends MethodOfPreservation("CHANGE OF RPA FROM COSR TO COMP (7)")
  case object ChangeOfRpaFromCosrToPp8         extends MethodOfPreservation("CHANGE OF RPA FROM COSR TO PP (8)")
  case object BuyoutOfGmp9                     extends MethodOfPreservation("BUYOUT OF GMP (9)")
  case object PseudoTransferNewEcon10          extends MethodOfPreservation("PSEUDO TRANSFER (NEW ECON) (10)")
  case object PseudoTransferPeriodAbroad11     extends MethodOfPreservation("PSEUDO TRANSFER (PERIOD ABROAD) (11)")
  case object AccruedRightsPremium12           extends MethodOfPreservation("ACCRUED RIGHTS PREMIUM (12)")
  case object BlankNotUsedOnNirs213            extends MethodOfPreservation("BLANK - NOT USED ON NIRS2 (13)")
  case object PensionersRightsPremium14        extends MethodOfPreservation("PENSIONERS RIGHTS PREMIUM (14)")
  case object ProtectedRightsPremium15         extends MethodOfPreservation("PROTECTED RIGHTS PREMIUM (15)")
  case object DummyTermination16               extends MethodOfPreservation("DUMMY TERMINATION (16)")
  case object TransferPremium17                extends MethodOfPreservation("TRANSFER PREMIUM (17)")
  case object PseudoTerminationDeathRecorded18 extends MethodOfPreservation("PSEUDO TERMINATION DEATH RECORDED (18)")
  case object CancellationOfPpCod19            extends MethodOfPreservation("CANCELLATION OF PP - COD (19)")
  case object TransferFromPpToCosr20           extends MethodOfPreservation("TRANSFER FROM PP TO COSR (20)")
  case object TransferFromPpToComp21           extends MethodOfPreservation("TRANSFER FROM PP TO COMP (21)")
  case object TransferFromPpToPp22             extends MethodOfPreservation("TRANSFER FROM PP TO PP (22)")

  case object TreatedAsChangeOfRpaFromPpToPp23
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM PP TO PP (23)")

  case object ChangeOfRpaFromPpToComp24 extends MethodOfPreservation("CHANGE OF RPA FROM PP TO COMP (24)")
  case object ChangeOfRpaFromPpToCosr25 extends MethodOfPreservation("CHANGE OF RPA FROM PP TO COSR (25)")

  case object ProvisionOfAPensionByThePpScheme26
      extends MethodOfPreservation("PROVISION OF A PENSION BY THE PP SCHEME (26)")

  case object PurchaseOfAnAnnuityPp27 extends MethodOfPreservation("PURCHASE OF AN ANNUITY - PP (27)")

  case object ProtectedRightsPaidInTheFormOfALumpSum28
      extends MethodOfPreservation("PROTECTED RIGHTS PAID IN THE FORM OF A LUMP SUM (28)")

  case object ProtectedRightsRetainedInCompSchemeCod29
      extends MethodOfPreservation("PROTECTED RIGHTS RETAINED IN COMP SCHEME - COD (29)")

  case object TransferFromCompToComp30    extends MethodOfPreservation("TRANSFER FROM COMP TO COMP (30)")
  case object TransferFromCompToCosr31    extends MethodOfPreservation("TRANSFER FROM COMP TO COSR (31)")
  case object TransferFromCompToPp32      extends MethodOfPreservation("TRANSFER FROM COMP TO PP (32)")
  case object ChangeOfRpaFromCompToComp33 extends MethodOfPreservation("CHANGE OF RPA FROM COMP TO COMP (33)")
  case object ChangeOfRpaFromCompToCosr34 extends MethodOfPreservation("CHANGE OF RPA FROM COMP TO COSR (34)")
  case object ChangeOfRpaFromCompToPp35   extends MethodOfPreservation("CHANGE OF RPA FROM COMP TO PP (35)")

  case object ProvisionOfAPensionByTheCompScheme36
      extends MethodOfPreservation("PROVISION OF A PENSION BY THE COMP SCHEME (36)")

  case object PurchaseOfAnAnnuityComp37 extends MethodOfPreservation("PURCHASE OF AN ANNUITY - COMP (37)")

  case object TreatedAsChangeOfRpaFromCosrToPp38
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM COSR TO PP (38)")

  case object TreatedAsChangeOfRpaFromCompToPp39
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM COMP TO PP (39)")

  case object BlankNotUsedOnNirs240 extends MethodOfPreservation("BLANK - NOT USED ON NIRS2 (40)")

  case object ChangeOfRpaFromCosrToPpConverted41
      extends MethodOfPreservation("CHANGE OF RPA FROM COSR TO PP (CONVERTED) (41)")

  case object ChangeOfRpaFromCompToPpConverted42
      extends MethodOfPreservation("CHANGE OF RPA FROM COMP TO PP (CONVERTED) (42)")

  case object ChangeOfRpaFromPpToPpConverted43
      extends MethodOfPreservation("CHANGE OF RPA FROM PP TO PP (CONVERTED) (43)")

  case object GuaranteedMinimumPensionCosrRights44
      extends MethodOfPreservation("GUARANTEED MINIMUM PENSION/COSR RIGHTS (44)")

  case object CosrRights45 extends MethodOfPreservation("COSR RIGHTS (45)")

  case object ProtectedRightsRetainedInCompSchemeCodPr46
      extends MethodOfPreservation("PROTECTED RIGHTS RETAINED IN COMP SCHEME - COD/PR (46)")

  case object ProtectedRightsRetainedInCompSchemePr47
      extends MethodOfPreservation("PROTECTED RIGHTS RETAINED IN COMP SCHEME - PR (47)")

  case object CancellationOfPpCodPr48 extends MethodOfPreservation("CANCELLATION OF PP - COD/PR (48)")
  case object CancellationOfPpPr49    extends MethodOfPreservation("CANCELLATION OF PP - PR (49)")
  case object CodCr50                 extends MethodOfPreservation("COD/CR (50)")
  case object CodCrPr51               extends MethodOfPreservation("COD/CR/PR (51)")
  case object GmpPr52                 extends MethodOfPreservation("GMP/PR (52)")
  case object GmpCrPr53               extends MethodOfPreservation("GMP/CR/PR (53)")
  case object CrPr54                  extends MethodOfPreservation("CR/PR (54)")
  case object RequiredAmountPremium55 extends MethodOfPreservation("REQUIRED AMOUNT PREMIUM (55)")
  case object IncomeWithdrawal56      extends MethodOfPreservation("INCOME WITHDRAWAL (56)")

  case object IncomeWithdrawalWidowWidowerSurvivingCivilPartner57
      extends MethodOfPreservation("INCOME WITHDRAWAL - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (57)")

  case object TransferGmpPrToCosr58           extends MethodOfPreservation("TRANSFER GMP/PR TO COSR (58)")
  case object TransferGmpPrToComp59           extends MethodOfPreservation("TRANSFER GMP/PR TO COMP (59)")
  case object TransferGmpPrToPp60             extends MethodOfPreservation("TRANSFER GMP/PR TO PP (60)")
  case object TransferCodCrToCosr61           extends MethodOfPreservation("TRANSFER COD/CR TO COSR (61)")
  case object TransferCodCrToComp62           extends MethodOfPreservation("TRANSFER COD/CR TO COMP (62)")
  case object TransferCodCrToPp63             extends MethodOfPreservation("TRANSFER COD/CR TO PP (63)")
  case object TransferGmpCrPrToCosr64         extends MethodOfPreservation("TRANSFER GMP/CR/PR TO COSR (64)")
  case object TransferGmpCrPrToComp65         extends MethodOfPreservation("TRANSFER GMP/CR/PR TO COMP (65)")
  case object TransferGmpCrPrToPp66           extends MethodOfPreservation("TRANSFER GMP/CR/PR TO PP (66)")
  case object TransferCodCrPrToCosr67         extends MethodOfPreservation("TRANSFER COD/CR/PR TO COSR (67)")
  case object TransferCodCrPrToComp68         extends MethodOfPreservation("TRANSFER COD/CR/PR TO COMP (68)")
  case object TransferCodCrPrToPp69           extends MethodOfPreservation("TRANSFER COD/CR/PR TO PP (69)")
  case object TransferCrPrToCosr70            extends MethodOfPreservation("TRANSFER CR/PR TO COSR (70)")
  case object TransferCrPrToComp71            extends MethodOfPreservation("TRANSFER CR/PR TO COMP (71)")
  case object TransferCrPrToPp72              extends MethodOfPreservation("TRANSFER CR/PR TO PP (72)")
  case object TreatedAsChangeOfRpaGmpPrToPp73 extends MethodOfPreservation("TREATED AS CHANGE OF RPA GMP/PR TO PP (73)")
  case object TreatedAsChangeOfRpaCodCrToPp74 extends MethodOfPreservation("TREATED AS CHANGE OF RPA COD/CR TO PP (74)")

  case object TreatedAsChangeOfRpaGmpCrPrToPp75
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA GMP/CR/PR TO PP (75)")

  case object TreatedAsChangeOfRpaCodCrPrToPp76
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA COD/CR/PR TO PP (76)")

  case object TreatedAsChangeOfRpaCrPrToPp77 extends MethodOfPreservation("TREATED AS CHANGE OF RPA CR/PR TO PP (77)")
  case object ChangeOfRpaFromPpToPp78        extends MethodOfPreservation("CHANGE OF RPA FROM PP TO PP (78)")
  case object OverseasTransfer79             extends MethodOfPreservation("OVERSEAS TRANSFER (79)")

  case object AssuranceTakingOutOfAnAppropriateInsurancePolicy80
      extends MethodOfPreservation("ASSURANCE/TAKING OUT OF AN APPROPRIATE INSURANCE POLICY (80)")

  case object ProvisionOfAPensionByThePpSchemeWidowWidowerSurvivingCivilPartner81
      extends MethodOfPreservation(
        "PROVISION OF A PENSION BY THE PP SCHEME - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (81)"
      )

  case object ProvisionOfAPensionByTheCompSchemeWidowWidowerSurvivingCivilPartner82
      extends MethodOfPreservation(
        "PROVISION OF A PENSION BY THE COMP SCHEME - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (82)"
      )

  case object PurchaseOfAnAnnuityPpWidowWidowerSurvivingCivilPartner83
      extends MethodOfPreservation("PURCHASE OF AN ANNUITY - PP - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (83)")

  case object PurchaseOfAnAnnuityCompWidowWidowerSurvivingCivilPartner84
      extends MethodOfPreservation("PURCHASE OF AN ANNUITY - COMP - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (84)")

  case object Commutation85 extends MethodOfPreservation("COMMUTATION (85)")

  case object CommutationWidowWidowerSurvivingCivilPartner86
      extends MethodOfPreservation("COMMUTATION - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (86)")

  case object InternalTransferCompRightsRetained87
      extends MethodOfPreservation("INTERNAL TRANSFER COMP RIGHTS RETAINED (87)")

  case object InternalTransferCosrRightsRetained88
      extends MethodOfPreservation("INTERNAL TRANSFER COSR RIGHTS RETAINED (88)")

  case object GmpSplitRights89              extends MethodOfPreservation("GMP SPLIT RIGHTS (89)")
  case object CodSplitRights90              extends MethodOfPreservation("COD SPLIT RIGHTS (90)")
  case object BlankNotUsedOnNirs291         extends MethodOfPreservation("BLANK - NOT USED ON NIRS2 (91)")
  case object BlankNotUsedOnNirs292         extends MethodOfPreservation("BLANK - NOT USED ON NIRS2 (92)")
  case object BlankNotUsedOnNirs293         extends MethodOfPreservation("BLANK - NOT USED ON NIRS2 (93)")
  case object BlankNotUsedOnNirs294         extends MethodOfPreservation("BLANK - NOT USED ON NIRS2 (94)")
  case object BlankNotUsedOnNirs295         extends MethodOfPreservation("BLANK - NOT USED ON NIRS2 (95)")
  case object CommutationIllHealth96        extends MethodOfPreservation("COMMUTATION-ILL HEALTH (96)")
  case object CancellationOfAppShpPr97      extends MethodOfPreservation("CANCELLATION OF APP SHP - PR (97)")
  case object TransferFromPpToAppShp98      extends MethodOfPreservation("TRANSFER FROM PP TO APP SHP (98)")
  case object TransferFromAppShpToPp99      extends MethodOfPreservation("TRANSFER FROM APP SHP TO PP (99)")
  case object TransferFromAppShpToAppShp100 extends MethodOfPreservation("TRANSFER FROM APP SHP TO APP SHP (100)")

  case object TreatedAsChangeOfRpaFromPpToAppShp101
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM PP TO APP SHP (101)")

  case object TreatedAsChangeOfRpaFromAppShpToPp102
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM APP SHP TO PP (102)")

  case object TreatedAsChangeOfRpaFromAppShpToAppShp103
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM APP SHP TO APP SHP (103)")

  case object ProtectedRightsRetainedInCompShpSchemeCodPr105
      extends MethodOfPreservation("PROTECTED RIGHTS RETAINED IN COMP SHP SCHEME - COD/PR (105)")

  case object ChangeOfRpaFromPpToCompShp106  extends MethodOfPreservation("CHANGE OF RPA FROM PP TO COMP SHP (106)")
  case object TransferFromPpToCompShp107     extends MethodOfPreservation("TRANSFER FROM PP TO COMP SHP (107)")
  case object ChangeOfRpaFromAppShpToComp108 extends MethodOfPreservation("CHANGE OF RPA FROM APP SHP TO COMP (108)")
  case object TransferFromAppShpToComp109    extends MethodOfPreservation("TRANSFER FROM APP SHP TO COMP (109)")

  case object ChangeOfRpaFromAppShpToCompShp110
      extends MethodOfPreservation("CHANGE OF RPA FROM APP SHP TO COMP SHP (110)")

  case object TransferFromAppShpToCompShp111  extends MethodOfPreservation("TRANSFER FROM APP SHP TO COMP SHP (111)")
  case object ChangeOfRpaFromAppShpToCosr112  extends MethodOfPreservation("CHANGE OF RPA FROM APP SHP TO COSR (112)")
  case object TransferFromAppShpToCosr113     extends MethodOfPreservation("TRANSFER FROM APP SHP TO COSR (113)")
  case object TransferFromCompToCompShp114    extends MethodOfPreservation("TRANSFER FROM COMP TO COMP SHP (114)")
  case object TransferFromCompShpToComp115    extends MethodOfPreservation("TRANSFER FROM COMP SHP TO COMP (115)")
  case object TransferFromCompShpToCompShp116 extends MethodOfPreservation("TRANSFER FROM COMP SHP TO COMP SHP (116)")
  case object TransferFromCompToAppShp117     extends MethodOfPreservation("TRANSFER FROM COMP TO APP SHP (117)")

  case object TreatedAsChangeOfRpaFromCompToAppShp118
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM COMP TO APP SHP (118)")

  case object TransferFromCompShpToPp119 extends MethodOfPreservation("TRANSFER FROM COMP SHP TO PP (119)")

  case object TreatedAsChangeOfRpaFromCompShpToPp120
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM COMP SHP TO PP (120)")

  case object TransferFromCompShpToAppShp121 extends MethodOfPreservation("TRANSFER FROM COMP SHP TO APP SHP (121)")

  case object TreatedAsChangeOfRpaFromCompShpToAppShp122
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM COMP SHP TO APP SHP (122)")

  case object TransferFromCompShpToCosr123 extends MethodOfPreservation("TRANSFER FROM COMP SHP TO COSR (123)")
  case object TransferFromCosrToCompShp124 extends MethodOfPreservation("TRANSFER FROM COSR TO COMP SHP (124)")
  case object TransferFromCosrToAppShp125  extends MethodOfPreservation("TRANSFER FROM COSR TO APP SHP (125)")

  case object TreatedAsChangeOfRpaFromCosrToAppShp126
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA FROM COSR TO APP SHP (126)")

  case object TransferCodCrToCompShp127   extends MethodOfPreservation("TRANSFER COD/CR TO COMP SHP (127)")
  case object TransferCodCrToAppShp128    extends MethodOfPreservation("TRANSFER COD/CR TO APP SHP (128)")
  case object TransferGmpCrPrToCompShp129 extends MethodOfPreservation("TRANSFER GMP/CR/PR TO COMP SHP (129)")
  case object TransferGmpCrPrToAppShp130  extends MethodOfPreservation("TRANSFER GMP/CR/PR TO APP SHP (130)")
  case object TransferCodCrPrToCompShp131 extends MethodOfPreservation("TRANSFER COD/CR/PR TO COMP SHP (131)")
  case object TransferCodCrPrToAppShp132  extends MethodOfPreservation("TRANSFER COD/CR/PR TO APP SHP (132)")
  case object TransferCrPrToCompShp133    extends MethodOfPreservation("TRANSFER CR/PR TO COMP SHP (133)")
  case object TransferCrPrToAppShp134     extends MethodOfPreservation("TRANSFER CR/PR TO APP SHP (134)")

  case object TreatedAsChangeOfRpaCodCrToAppShp135
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA COD/CR TO APP SHP (135)")

  case object TreatedAsChangeOfRpaGmpCrPrToAppShp136
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA GMP/CR/PR TO APP SHP (136)")

  case object TreatedAsChangeOfRpaCodCrPrToAppShp137
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA COD/CR/PR TO APP SHP (137)")

  case object TreatedAsChangeOfRpaCrPrToAppShp138
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA CR/PR TO APP SHP (138)")

  case object ChangeOfRpaFromCosrToCompShp140 extends MethodOfPreservation("CHANGE OF RPA FROM COSR TO COMP SHP (140)")
  case object ChangeOfRpaFromCosrToAppShp141  extends MethodOfPreservation("CHANGE OF RPA FROM COSR TO APP SHP (141)")
  case object ChangeOfRpaFromCompShpToComp142 extends MethodOfPreservation("CHANGE OF RPA FROM COMP SHP TO COMP (142)")
  case object ChangeOfRpaFromCompShpToPp143   extends MethodOfPreservation("CHANGE OF RPA FROM COMP SHP TO PP (143)")

  case object ChangeOfRpaFromCompShpToCompShp144
      extends MethodOfPreservation("CHANGE OF RPA FROM COMP SHP TO COMP SHP (144)")

  case object ChangeOfRpaFromCompShpToCosr145 extends MethodOfPreservation("CHANGE OF RPA FROM COMP SHP TO COSR (145)")

  case object ChangeOfRpaFromCompShpToAppShp146
      extends MethodOfPreservation("CHANGE OF RPA FROM COMP SHP TO APP SHP (146)")

  case object ProtectedRightsRetainedInCompShpSchemePr150
      extends MethodOfPreservation("PROTECTED RIGHTS RETAINED IN COMP SHP SCHEME - PR (150)")

  case object ProvisionOfAPensionByTheAppShpScheme152
      extends MethodOfPreservation("PROVISION OF A PENSION BY THE APP SHP SCHEME (152)")

  case object ProvisionOfAPensionByTheAppShpSchemeWidowWidowerSurvivingCivilPartner153
      extends MethodOfPreservation(
        "PROVISION OF A PENSION BY THE APP SHP SCHEME - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (153)"
      )

  case object PurchaseOfAnAnnuityAppShp154 extends MethodOfPreservation("PURCHASE OF AN ANNUITY - APP SHP (154)")

  case object PurchaseOfAnAnnuityAppShpWidowWidowerSurvivingCivilPartner155
      extends MethodOfPreservation("PURCHASE OF AN ANNUITY - APP SHP - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (155)")

  case object TreatedAsChangeOfRpaGmpPrToAppShp156
      extends MethodOfPreservation("TREATED AS CHANGE OF RPA GMP/PR TO APP SHP (156)")

  case object CancellationOfAppShpCodPr157 extends MethodOfPreservation("CANCELLATION OF APP SHP - COD/PR (157)")

  case object ProvisionOfAPensionByTheCompShpScheme158
      extends MethodOfPreservation("PROVISION OF A PENSION BY THE COMP SHP SCHEME (158)")

  case object ProvisionOfAPensionByTheCompShpSchemeWidowWidowerSurvivingCivilPartner159
      extends MethodOfPreservation(
        "PROVISION OF A PENSION BY THE COMP SHP SCHEME - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (159)"
      )

  case object PurchaseOfAnAnnuityCompShp160 extends MethodOfPreservation("PURCHASE OF AN ANNUITY - COMP SHP (160)")

  case object PurchaseOfAnAnnuityCompShpWidowWidowerSurvivingCivilPartner161
      extends MethodOfPreservation("PURCHASE OF AN ANNUITY - COMP SHP - WIDOW/WIDOWER/SURVIVING CIVIL PARTNER (161)")

  case object ChangeOfRpaFromPpToAppShp162 extends MethodOfPreservation("CHANGE OF RPA FROM PP TO APP SHP (162)")
  case object ChangeOfRpaFromAppShpToPp163 extends MethodOfPreservation("CHANGE OF RPA FROM APP SHP TO PP (163)")

  case object ChangeOfRpaFromAppShpToAppShp164
      extends MethodOfPreservation("CHANGE OF RPA FROM APP SHP TO APP SHP (164)")

  case object ChangeOfRpaFromCompToCompShp165 extends MethodOfPreservation("CHANGE OF RPA FROM COMP TO COMP SHP (165)")
  case object ChangeOfRpaFromCompToAppShp166  extends MethodOfPreservation("CHANGE OF RPA FROM COMP TO APP SHP (166)")
  case object TransferGmpPrToCompShp167       extends MethodOfPreservation("TRANSFER GMP/PR TO COMP SHP (167)")
  case object TransferGmpPrToAppShp168        extends MethodOfPreservation("TRANSFER GMP/PR TO APP SHP (168)")
  case object BuyoutOfProtectedRights169      extends MethodOfPreservation("BUYOUT OF PROTECTED RIGHTS (169)")
  case object ChangeOfRpaToPpf170             extends MethodOfPreservation("CHANGE OF RPA TO PPF (170)")
  case object ChangeOfRpaToFas171             extends MethodOfPreservation("CHANGE OF RPA TO FAS (171)")
}
