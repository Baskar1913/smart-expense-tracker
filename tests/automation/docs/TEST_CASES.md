# Smart Expense Tracker – Automated Test Cases

## Scope

The suite automates the customer-only React and Spring Boot application.

- UI automation: Selenium WebDriver, Java, Page Object Model.
- API automation: REST Assured, Java.
- Runner and assertions: JUnit 5.

## UI test cases

| ID | Module | Scenario | Expected result | Automation |
|---|---|---|---|---|
| UI-AUTH-01 | Login | Open login page | Username/password are blank and development credentials are not shown | `AuthenticationUiTests.loginFieldsAreBlank` |
| UI-AUTH-02 | Registration/Login | Register a unique customer and sign in | Account is created and dashboard opens | `customerCanRegisterAndLogin` |
| UI-AUTH-03 | Registration | Submit different password and confirmation | “Passwords do not match” is displayed | `registrationRejectsMismatchedPasswords` |
| UI-AUTH-04 | Login | Submit invalid credentials | Invalid-credentials message is displayed | `invalidLoginDisplaysError` |
| UI-AUTH-05 | Forgot password | Check a username that does not exist | Account-not-found page and Create account option appear | `unknownForgotPasswordOffersCreateAccount` |
| UI-AUTH-06 | Forgot password | Verify email, reset password and login | Password resets and new password authenticates | `existingCustomerCanResetPasswordAndLogin` |
| UI-AUTH-07 | Brute-force security | Fail one username five times, then login as another | First username is blocked; second username can still login | `blockingOneUsernameDoesNotBlockSecondUsername` |
| UI-EXP-01 | Expense | Verify blank category and add an expense | Expense appears; ID column is not displayed | `addExpenseWithoutShowingId` |
| UI-EXP-02 | Search | Search by title/category/date | Only matching record is shown | `searchAndFilterExpenses` |
| UI-EXP-03 | Summary cards | Add expenses and reload | Total spend shows the calculated amount | `totalSpendIsUpdated` |
| UI-EXP-04 | Monthly analytics | Select year/month and load summary | Correct `.txt` file is downloaded with summary content | `monthlySummaryDownloadsTextFile` |
| UI-EXP-05 | Delete | Delete an owned expense and confirm | Record disappears from the table | `deleteExpense` |
| UI-EXP-06 | Logout | Click Log out | Login screen is shown and session is cleared | `logoutReturnsToLogin` |

## API test cases

| ID | Module | Endpoint/Scenario | Expected result | Automation |
|---|---|---|---|---|
| API-AUTH-01 | Registration | `POST /auth/register` valid data | HTTP 201 and success message | `AuthApiTests.registerNewCustomer` |
| API-AUTH-02 | Registration | Duplicate username | HTTP 400, username already exists | `rejectDuplicateUsername` |
| API-AUTH-03 | Validation | Invalid username/email/password | HTTP 400 with validation errors | `validateRegistrationInput` |
| API-AUTH-04 | Login | Valid credentials | HTTP 200 with access and refresh JWTs | `loginReturnsTokens` |
| API-AUTH-05 | Login | Invalid password | HTTP 401 | `rejectInvalidPassword` |
| API-AUTH-06 | Rate limiting | Five failures for one user, login as another | First user gets HTTP 429 on next request; second user gets HTTP 200 | `failedAttemptsForOneUsernameDoNotBlockAnotherUsername` |
| API-AUTH-07 | Forgot password | Unknown username | HTTP 200 with `exists=false` | `forgotPasswordCheckUnknownUser` |
| API-AUTH-08 | Password reset | Verify email, reset password | New password logs in; old password fails | `verifyAndResetPassword` |
| API-AUTH-09 | Refresh | Rotate refresh token and reuse old token | New pair returned; old refresh token rejected | `refreshTokenRotation` |
| API-AUTH-10 | Logout | Logout and reuse access token | Logout succeeds; revoked token gets HTTP 401 | `logoutRevokesAccessToken` |
| API-EXP-01 | Security | `GET /expenses` without token | HTTP 401 | `protectedEndpointRequiresAuthentication` |
| API-EXP-02 | Create | `POST /expenses` valid record | HTTP 201 and normalized category | `createExpense` |
| API-EXP-03 | Data isolation | Two users list expenses | Each customer sees only their own records | `listOnlyOwnExpenses` |
| API-EXP-04 | Category filter | `GET /expenses?category=Food` | Only Food records returned | `filterExpensesByCategory` |
| API-EXP-05 | Search | Query/category/date/amount filters | Only matching record returned | `searchExpenses` |
| API-EXP-06 | Totals | Overall total and category total | Correct monetary totals | `calculateTotals` |
| API-EXP-07 | Category totals | `GET /expenses/total/by-category` | Correct grouped totals | `totalsGroupedByCategory` |
| API-EXP-08 | Monthly summary | July data plus August data | July count and total exclude August | `monthlySummary` |
| API-EXP-09 | Delete | Delete own expense | HTTP 200; record no longer listed | `deleteOwnedExpense` |
| API-EXP-10 | Authorization | User B deletes user A expense | HTTP 404 | `preventDeletingAnotherCustomersExpense` |
| API-EXP-11 | Validation | Amount is zero | HTTP 400 | `rejectInvalidExpense` |
| API-EXP-12 | Search validation | From date later than To date | HTTP 400 | `rejectInvalidSearchRange` |
| API-EXP-13 | Monthly validation | Month 13 | HTTP 400 | `rejectInvalidMonth` |

## Entry and exit criteria

**Entry criteria**

- Backend is running and reachable.
- Frontend is running for UI tests.
- File-based H2 database is writable.
- Browser is installed.

**Exit criteria**

- All high-priority authentication, ownership, create/list/search/total/delete and monthly-download tests pass.
- Any failure has a Maven report and, for UI cases, a screenshot.
