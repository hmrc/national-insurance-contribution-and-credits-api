
# national-insurance-contribution-and-credits-api

The National Insurance Contribution and Credits API aims to provide an automated data exchange mechanism between third parties and HMRC to support necessary Bereavement Support Payment  eligibility checks. In a nutshell, this API allows developers to retrieve Class 1 and Class 2 data for an account with a request payload which contain start tax year, end tax year, national insurance number and date of birth.

### How to run the service
You can run the service using service manager with profile `NATIONAL_INSURANCE_CONTRIBUTION_AND_CREDITS_ALL` `NATIONAL_INSURANCE_CONTRIBUTION_AND_CREDITS` or locally with `sbt "run 16105"`

## Run Tests

Run unit tests: `sbt test`

## Endpoints

`POST /contribution-and-credits`

Will return niClass1 and/or niClass2 collections

Request:

| Parameter               | Description                                  | Mandatory        |
|:------------------------|:---------------------------------------------|:-----------------|
| startTaxYear            | Denotes a start year for the tax year range. | yes              |
| endTaxYear              | Denotes a end year for the tax year range.   | yes              |
| nationalInsuranceNumber | NINO - unique for an individual.             | yes              |
| dateOfBirth             | The date of birth of the individual.         | yes              |
| customerCorrelationID   | Correlation Id sent by customer.             | optional         |


Example:
```json
{
  "startTaxYear": "2018",
  "endTaxYear": "2023",
  "nationalInsuranceNumber": "BB000200B",
  "dateOfBirth": "1970-08-31",
  "customerCorrelationID": "a3cf583f-9a4a-4587-bbd6-05e7e30bb7ee"
}
```

If "affinityGroup" is either Individual or Organisation and the "credentialStrength" is weak or none, a 400/BadRequest will be returned.

Returns:

HTTP 200, `correlationId` header containing correlationId originating from app throughout request the request journey, body:
```json
{
  "niClass1": [
    {
      "taxYear": 2022,
      "niContributionCategory": "^A$",
      "niContributionCategoryName": "(NONE)",
      "niContributionType": "C1",
      "totalPrimaryContribution": 99999999999999.98,
      "contributionStatus": "COMPLIANCE & YIELD INCOMPLETE",
      "totalEarningsFactor": 99999999999999.98
    }
  ],
  "niClass2": [
    {
      "taxYear": 2022,
      "numberOfWeeks": 53,
      "niContributionType": "C1",
      "totalEarningsFactor": 99999999999999.98,
      "totalPrimaryContribution": 99999999999999.98,
      "contributionStatus": "NOT KNOWN/NOT APPLICABLE"
    }
  ]
}
```

HTTP 400, 403, 422, `correlationId` header containing correlationId originating from app throughout request the request journey, body:

```json
{
  "failures": [
    {
      "reason": "Start tax year after end tax year",
      "code": "63496"
    }
  ]
}
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").