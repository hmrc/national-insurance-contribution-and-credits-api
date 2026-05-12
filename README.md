# National Insurance Contribution and Credits API

The National Insurance Contribution and Credits API provides an automated data exchange mechanism between third parties and HMRC to support eligibility checks for various Department for Work and Pensions (DWP) benefits. The API allows developers to retrieve National Insurance contribution and credits data along with benefit eligibility information.

## Overview

The API operates through multiple endpoints to facilitate eligibility verification across five distinct benefit categories:

- **MA** - Maternity Allowance
- **JSA** - Job Seekers Allowance
- **ESA** - Employment Support Allowance
- **BSP** - Bereavement Support Payment
- **GYSP** - Get Your State Pension

**Note**: Bereavement Support Payment (BSP) data may also be requested through **Searchlight** infrastructure, identified as **BSP_SEARCHLIGHT**.

The enhanced service architecture is capable of processing and responding to requests for the following benefit types: MA, JSA, ESA, BSP, BSP_SEARCHLIGHT, and GYSP.

## How to Run the Service

You can run the service using service manager with profile `NATIONAL_INSURANCE_CONTRIBUTION_AND_CREDITS_ALL` or `NATIONAL_INSURANCE_CONTRIBUTION_AND_CREDITS`, or locally with:

```bash
sbt "run 16105"
```

## Run Tests

Run unit tests:

```bash
sbt test
```

## Endpoints

### POST /contributions-and-credits

Returns National Insurance Class 1 and/or Class 2 collections for a specified tax year range.

**Request Parameters:**

| Parameter               | Description                                  | Mandatory |
|:------------------------|:---------------------------------------------|:----------|
| startTaxYear            | Start year for the tax year range             | Yes       |
| endTaxYear              | End year for the tax year range               | Yes       |
| nationalInsuranceNumber | NINO - unique identifier for an individual    | Yes       |
| dateOfBirth             | Date of birth of the individual               | Yes       |
| customerCorrelationID   | Correlation ID sent by customer               | Optional  |

**Example Request:**

```json
{
  "startTaxYear": "2018",
  "endTaxYear": "2023",
  "nationalInsuranceNumber": "BB000200B",
  "dateOfBirth": "1970-08-31",
  "customerCorrelationID": "a3cf583f-9a4a-4587-bbd6-05e7e30bb7ee"
}
```

**Success Response (HTTP 200):**

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

**Error Response (HTTP 400, 403, 422):**

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

All responses include a `correlationId` header for request tracing throughout the request journey.

---

### POST /benefit-eligibility-info

Retrieves benefit eligibility information for DWP benefit verification processes. This endpoint processes requests for various benefit types and returns consolidated eligibility data.

**Supported Benefit Types:** MA, JSA, ESA, BSP, GYSP
in addition to the above benefit types this service also supports SEARCHLIGHT requests for BSP

**Request Parameters:**

# Request Parameters Table (BSP, JSA, ESA)

**POST /benefit-eligibility-info**

| Parameter                                    | Type    | Description                                                 | Mandatory |
|:---------------------------------------------|:--------|:------------------------------------------------------------|:----------|
| benefitType                                  | String  | Type of benefit for eligibility check (BSP, JSA, ESA)       | Yes       |
| nationalInsuranceNumber                      | String  | NINO - unique identifier for an individual                  | Yes       |
| niContributionsAndCredits                    | Object  | Container for National Insurance contribution data          | Yes       |
| niContributionsAndCredits.dateOfBirth        | String  | Date of birth of the individual (format: YYYY-MM-DD)        | Yes       |
| niContributionsAndCredits.startTaxYear       | Integer | Start year for the tax year range                           | Yes       |
| niContributionsAndCredits.endTaxYear         | Integer | End year for the tax year range                             | Yes       |


```json
{
  "benefitType": "BSP",
  "nationalInsuranceNumber": "NP012345Q",
  "niContributionsAndCredits": {
    "dateOfBirth": "1988-07-25",
    "startTaxYear": 2021,
    "endTaxYear": 2023
  }
}
```

# Request Parameters Table (MA)

| Parameter                                    | Type    | Description                                                         | Mandatory |
|:---------------------------------------------|:--------|:--------------------------------------------------------------------|:----------|
| benefitType                                  | String  | Type of benefit for eligibility check (MA)                          | Yes       |
| nationalInsuranceNumber                      | String  | NINO - unique identifier for an individual                          | Yes       |
| niContributionsAndCredits                    | Object  | Container for National Insurance contribution data                  | Yes       |
| niContributionsAndCredits.dateOfBirth        | String  | Date of birth of the individual (format: YYYY-MM-DD)                | Yes       |
| niContributionsAndCredits.startTaxYear       | Integer | Start year for the tax year range                                   | Yes       |
| niContributionsAndCredits.endTaxYear         | Integer | End year for the tax year range                                     | Yes       |
| liabilities                                  | Object  | Container for National Insurance liability data                     | Yes       |
| liabilities.searchCategories                 | Array   | List of liability categories to search (e.g., CLASS-2-LIABILITY-UK) | Yes       |
| liabilities.earliestLiabilityStartDate       | String  | Earliest liability start date (format: YYYY-MM-DD)                  | No        |
| liabilities.liabilityStart                   | String  | Liability period start date (format: YYYY-MM-DD)                    | No        |
| liabilities.liabilityEnd                     | String  | Liability period end date (format: YYYY-MM-DD)                      | No        |

**Example Request:**

```json
{
  "benefitType": "MA",
  "nationalInsuranceNumber": "NP012345Q",
  "niContributionsAndCredits": {
    "dateOfBirth": "1988-07-25",
    "startTaxYear": 2021,
    "endTaxYear": 2023
  },
  "liabilities": {
    "searchCategories": [
      "CLASS-2-LIABILITY-UK"
    ],
    "earliestLiabilityStartDate": "2021-01-01",
    "liabilityStart": "2021-06-15",
    "liabilityEnd": "2022-06-14"
  }
}
```

**Example Request:**

```json
{
  "benefitType": "BSP",
  "nationalInsuranceNumber": "CD789012E",
  "niContributionsAndCredits": {
    "dateOfBirth": "1958-03-22",
    "startTaxYear": 2018,
    "endTaxYear": 2024
  }
}
```

# Request Parameters Table SEARCHLIGHT

| Parameter                                    | Type    | Description                                                | Mandatory |
|:---------------------------------------------|:--------|:-----------------------------------------------------------|:----------|
| system                                       | String  | System identifier for request routing (SEARCHLIGHT)        | Yes       |
| benefitType                                  | String  | Type of benefit for eligibility check (BSP)                | Yes       |
| nationalInsuranceNumber                      | String  | NINO - unique identifier for an individual                 | Yes       |
| niContributionsAndCredits                    | Object  | Container for National Insurance contribution data         | Yes       |
| niContributionsAndCredits.dateOfBirth        | String  | Date of birth of the individual (format: YYYY-MM-DD)       | Yes       |
| niContributionsAndCredits.startTaxYear       | Integer | Start year for the tax year range                          | Yes       |
| niContributionsAndCredits.endTaxYear         | Integer | End year for the tax year range                            | Yes       |

**Example Request:**

```json
{
  "system": "SEARCHLIGHT",
  "benefitType": "BSP",
  "nationalInsuranceNumber": "CD789012E",
  "niContributionsAndCredits": {
    "dateOfBirth": "1958-03-22",
    "startTaxYear": 2018,
    "endTaxYear": 2024
  }
}
```

**Data Processing Workflow:**

Upon receipt of a request for any benefit type, the system initiates a data processing workflow that involves making simultaneous calls to various National Insurance and PAYE Service (NPS) endpoints. The specific endpoints accessed depend on the benefit type requested:

**For benefitTypes ESA, JSA and for SEARCHLIGHT requests:**
- Credits and Contributions endpoint

**For MA (Maternity Allowance) benefit type:**
- Credits and Contributions
- Liability Summary Details

**For BSP (Bereavement Support Payment) benefit type:**
- Credits and Contributions
- Marriage Details

**For GYSP (Get Your State Pension) benefit type:**
- Credits and Contributions
- Marriage Details
- Scheme Membership Details
- Benefit Scheme Details
- Individual State Pension
- Long-term Benefit Calculation Details
- Long-term Benefit Calculation Notes

Following successful data retrieval from all relevant NPS endpoints, the system applies appropriate filtering mechanisms to extract only the required fields and formats the response for transmission back to DWP.

---

### GET /benefit-eligibility-info?cursorId=\<someId\>

Retrieves paginated benefit eligibility data using a cursor reference. Use this endpoint when a previous POST request returned a `nextCursor` value, indicating additional data is available.

**Query Parameters:**

| Parameter  | Description                                    | Mandatory |
|:-----------|:-----------------------------------------------|:----------|
| nextCursor | Cursor reference for retrieving paginated data | Yes       |


See OAS documentation for further detail (application.yaml)

## Pagination Management and Data Continuity

Given that certain NPS endpoints may return data in a paginated format, the service implements a pagination delegation strategy that transfers the responsibility for managing paginated responses to DWP. This architectural decision ensures that if NPS returns incomplete data due to pagination constraints, DWP will receive correspondingly incomplete data along with the necessary tools to retrieve the remaining information.

When incomplete data is returned, the response will include a special identifier called **nextCursor**, which contains a reference to a MongoDB document storing all relevant pagination information for the remaining data. To retrieve additional data, DWP must include this nextCursor value as a query parameter in subsequent calls to the **GET /benefit-eligibility-info** endpoint.

A complete response containing all requested data will not include a `nextCursor` field, indicating that no additional data retrieval is necessary.

**Request types that may return paginated (incomplete) data responses:**
- BSP
- MA
- GYSP
- SEARCHLIGHT

## NPS Request Processing Considerations

All NPS requests for benefit types requiring data from multiple NPS sources (specifically MA, GYSP, and BSP) are executed in parallel to optimize performance and reduce response times.

### GYSP Interdependencies

GYSP processing includes interdependent NPS endpoint calls where responses from certain endpoints influence parameters for subsequent requests:

1. **Long-term Benefit Calculation Dependency**: The `associatedCalculationSequenceNumber` field extracted from the Long-term Benefit Calculation response is utilized as a parameter for the subsequent call to the Long-term Benefit Calculation Notes endpoint.

2. **Scheme Membership Dependency**: SCON (Scheme Contracted-Out Number) values retrieved from the Scheme Membership Details response—specifically `schemeCreatingContractedOutNumberDetails` or `schemeTerminatingContractedOutNumberDetails` (or both if they exist in the response and contain different values)—are used to make the necessary calls to the Benefit Scheme Details endpoint.

## License

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
