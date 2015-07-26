package models


import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.data._
import play.api.data.validation.Constraints.pattern
import play.api.libs.json._

case class SupType(s1: Int, s2: Int, s3: Int)

case class SubType(

                  s11: Int,
                  s12: Int,
                  s21: Int,
                  s22: Int,
                  s31: Int,
                  s32: Int
                      )

case class GroupP(
                s111: Int,
                s112: Int,
                s121: Int,
                s122: Int,

                s211: Int,
                s212: Int,
                s221: Int,
                s222: Int,

                s311: Int,
                s312: Int,
                s321: Int,
                s322: Int
                    )

case class Brand(
                s1: Int,
                s2: Int,
                s3: Int,
                s4: Int,
                s5: Int,
                s6: Int,
                s7: Int,
                s8: Int,
                s9: Int,
                s10: Int,
                s11: Int,
                s12: Int,
                s13: Int,
                s14: Int
                    )

case class Origin(
                 s1: Int,
                 s2: Int,
                 s3: Int,
                 s4: Int,
                 s5: Int
                     )

case class LegType(
                    s1: Int,
                    s2: Int,
                    s3: Int,
                    s4: Int,
                    s5: Int,
                    s6: Int,
                    s7: Int,
                    s8: Int,
                    s9: Int,
                    s10: Int,
                    s11: Int
                    )

case class LegNumber(
                    s1: Int,
                    s2: Int,
                    s3: Int,
                    s4: Int,
                    s5: Int,
                    s6: Int,
                    s7: Int,
                    s8: Int,
                    s9: Int,
                    s10: Int,
                    s11: Int,
                    s12: Int,
                    s13: Int,
                    s14: Int,
                    s15: Int
                    )
