package object model {
  abstract sealed class Importance(val value: String)
  case object High extends Importance("high")
  case object Medium extends Importance("medium")
  case object Low extends Importance("low")

  object Importance {
    private def values = Set(High, Medium, Low)

    def unsafeFromString(value: String): Importance = {
      values.find(_.value == value).get
    }
  }

  case class Todo(id: Option[Long], description: String, importance: Importance)

  case object TodoNotFoundError

  case class SourceCode(id: Option[Long], sourceCode: String, mid13: String, mid10: String, first2: String, activityType: String,
                        activitySource: String, activitySource2: String, activitySource3: String, activitySource4: String,
                        activitySource5: String, campaignNo: Option[String], advertisedRate: Double, brandCode: String, pct: String, end2: String)

  case class SourceCodeLookup(sourceCode: String)
  case class SourceCodeExists(exists: Boolean)
  case object SourceCodeNotFound
  case object SourceCodeNotFoundError
  case object SourceCodeExistsError
  case class BrandValues(brand: String, optionValue: String)

  case class SourceCodeVerificationResponse(activityType: String, activitySource1: String, activitySource2: String,
                                            activitySource3: List[String], brandCode: String, pct: String)
}
