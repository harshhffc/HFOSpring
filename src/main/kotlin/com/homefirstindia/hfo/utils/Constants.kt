package com.homefirstindia.hfo.utils

import com.homefirstindia.hfo.clients.EnS3BucketPath
import org.apache.http.entity.ContentType

const val SYSTEM_USER_ID = "a88eba44-e0c2-49a4-b7d1-afc13a03e19d"
const val DEFAULT_PARENT_ORG_ID = "homefirst"
const val DEFAULT_PARTNER_ID = "homefirst"
const val DEFAULT_LEAD_SOURCE = "homefirst"
const val TU_NEXT_STEP = "TU_NEXT_STEP"
const val HFO_ID = "hfoId"
const val REFERENCE_ID = "referenceId"

const val SERVICE_INSTANT_TOP_UP = "INSTANT_TOP_UP"

const val RETRY_THRESHOLD = 3
const val RETRY_INCRETMENT_THRESHOLD = 2
const val HITBERNATE_BATCH_SIZE = 25

const val E_MANDATE = "E-Mandate"
const val SF_NONE = "--None--"

const val UTF_8 = "UTF-8"
const val NA = "NA"
const val OK = "OK"
const val RESET = "RESET"
const val NONE = "NONE"
const val STATUS = "status"
const val STATUS_CODE_200 = "200"
const val X_API_KEY = "x-Api-key"
const val SUCCESS = "success"
const val FAILURE = "failure"
const val DELIVRD = "DELIVRD"
const val MESSAGE = "message"
const val ADDITIONAL_MESSAGE = "additionalMessage"
const val ERROR = "error"
const val REASON = "reason"
const val ACTION = "action"
const val TOKEN = "token"
const val REFERSH_TOKEN = "refreshToken"
const val FAILURE_JSON_STRING = "{\"status\": \"failure\"}"
const val SUCCESS_JSON_STRING = "{\"status\": \"success\"}"
const val SESSION_PASSCODE = "sessionPasscode"
const val VALID_UPTO = "validUpto"
const val ALLOWED_SERVICES = "allowedServices"
const val SOURCE_PASSCODE = "sourcePasscode"
const val REQUEST_TOKEN = "requestToken"
const val AUTHORIZATION = "Authorization"
const val CONTENT_TYPE = "content-type"
const val AUTHORIZED = "AUTHORIZED"
const val CLIENT_CODE = "clientCode"
const val ORG_ID = "orgId"
const val PARTNER_ORG_ID = "partnerOrgId"
const val CONNECTOR_ID = "connectorId"
const val LEAD_ID = "leadId"
const val TASK_ID = "taskId"
const val TASK = "task"
const val LEAD = "lead"
const val CAMPAIGN = "Campaign"
const val LEADSOURCE = "leadSource"
const val LEAD_SCORE = "leadScore"
const val LEAD_HISTORY = "leadHistory"
const val APPLICANT = "applicant"
const val SF_LEAD_ID = "sfLeadId"
const val REQUEST_ID = "requestId"
const val USER_ID = "userId"
const val EMAIL_ID = "emailId"

const val FILE_NAME = "fileName"

const val FILE_DATA = "fileData"
const val ID = "id"
const val USER = "user"
const val ROLES = "roles"
const val TEAMS = "teams"
const val ORGANIZATION = "Organization"
const val SALES_GROUPS = "salesGroups"
const val USER_REQUEST = "userRequest"
const val DEFAULT_ERROR_MESSAGE = "Something went wrong.Please try again!"
const val IS_ENABLED = "isEnabled"
const val ACCOUNT_EXPIRED = "accountExpired"
const val ACCOUNT_LOCKED = "accountLocked"
const val CREDENTIALS_EXPIRED = "credentialsExpired"
const val CALLER_ID = "caller_id"

const val CLIENT_ID = "clientId"
const val CLIENT_SECRET = "clientSecret"


const val FALLBACK_LOAN_AMOUNT = 11

const val DEVICE_TYPE = "deviceType"
const val ANDROID = "android"
const val iOS = "iOS"
const val TYPE = "type"

const val OPEN = "open"
const val CLOSED = "closed"
const val CUSTOMER = "customer"
const val MANDATE_USER = "mandateUser"
const val CROWN_PASSCODE = "crownPasscode"
const val CRON = "cron"
const val EXTERNAL_PARTNER = "externalPartner"
const val INTERNAL_USER = "internalUser"
const val PORTAL_USER = "portalUser"
const val INTERMEDIATE_SERVICES = "intermediateServices"
const val PUBLIC_UESR = "publicUser"

const val DUPLICATES_DETECTED = "DUPLICATES_DETECTED"
const val STRING_TOO_LONG = "STRING_TOO_LONG"
const val JSON_PARSER_ERROR = "JSON_PARSER_ERROR"

const val LOAN_TYPE_CLOSED = "Closed - Obligations met"
const val LOAN_TYPE_CANCELED = "Canceled"
const val LOAN_TYPE_CANCELLED = "Cancelled"

const val CONTENT_TYPE_APPLICATION_JSON = "application/json"

const val USER_RABIT = "0059000000UuZ18AAF"
const val USER_API = "00590000006VHPKAA4"

const val USER_PRIYADARSHINI = "0059000000TotWlAAJ"
const val EMAIL_PRIYADARSHINI = "priyadharshini.pk@homefirstindia.com"
const val USER_ASMI = "0050o00000WRnCTAA1"
const val USER_SAKSHI = "0059000000ThA5KAAV"
const val EMAIL_SAKSHI = "sakshi.garg@homefirstindia.com"
const val BRANCH_HO = "a039000000F06xbAAB"

const val DATE_FILTER = "dateFilter"
const val START_DATETIME = "startDatetime"
const val END_DATETIME = "endDatetime"

const val ROLE_ALL = "ALL"
const val ALL = "ALL"
const val CONSENT_TYPE = "all"

const val OTP = "OTP"

const val DYNAMIC = "Dynamic"

const val COMMON_KEY_REQUEST_TYPE = "requestType"

const val OTP_VALIDITY_MINUTES = 15

const val DEFAULT_BRANCH_ID = "a039000000F06xbAAB"
const val DEFAULT_BRANCH_NAME = "Head Office"
const val DEFAULT_BRANCH_ADDRESS = "511 Acme Plaza, Andheri Kurla Road, Opposite Sangam Cinema, Andheri East"
const val DEFAULT_BRANCH_PRIMARY_CONTACT = "02228321007"
const val DEFAULT_BRANCH_SECONDARY_CONTACT = "02228321017"

const val DEFAULT_OWNER_ID = "00590000002FNJaAAO"
const val DEFAULT_OWNER_NAME = "Joyce DCosta"
const val DEFAULT_OWNER_EMAIL = "joyce.dcosta@homefirstindia.com"
const val DEFAULT_OWNER_MOBILE_NO = "9769903150"

const val NO_CREDIT_SCORE = -59
const val LEAD_EXPORT_VALUE = 100000
const val NO_DECIMAL_NUMBER = -1.0
const val NO_NUMBER = -1

const val QUERY_ID_REQUEST_PARAM = "{query_id}"
const val FILE_NAME_REQUEST_PARAM = "{filename}"

const val MOBILE_NUMBER = "mobileNumber"

const val DEFAULT_CONNECTOR_SOURCE = "Other"
const val DEFAULT_RELIGION = "Others"

const val CANCEL_CHEQUE = "cancelledCheque"
const val PASSBOOK = "passbook"
const val CONNECTOR = "connector"
const val BANK_INFO = "bankInfo"
const val KYC_DOCUMENTS = "kycDocuments"
const val AGREEMENT_INFO = "agreementInfo"

const val PARTNER_TRANS_UNION = "TransUnion"
const val PARTNER_PAYNIMO_MANDATE = "PaynimoMandate"
const val PARTNER_FIRBASE_SHORT_URL = "FB_Short_Link"
const val PARTNER_RAZORPAY = "Razorpay"
const val PARTNER_TEAL = "Teal"

const val EXISTS = "exists"

const val CYPHER = "cypher"
const val CRYPT = "crypt"

const val PROFILE_STATUS = "profileStatus"
const val PROMOTION = "promotion"

const val AUTH_TWO = "AUTH_TWO"

const val OLD_CONNECTOR_DECLINE_STAGE = "0-Declined"
const val THREAD_POOL_TASK_EXECUTOR = "threadPoolTaskExecutor"
const val NOTIFICATION = "Notification"

const val PARTNER_HOMEFIRST_LMS = "HomefirstLMS"

//====== Notification Constants ======/

const val FIREBASE_PROJECT_NAME = "LMS22"

//==================================//

//======CALL Constants======//

const val CHECK_IN = "checkIn"
const val CHECK_OUT = "checkOut"

const val CHECK_IN_DETAILS = "checkInDetails"
const val TEAM_CALLER_INFO = "teamCallerInfo"

const val ITR_AVAILABLE = "ITR Available"
const val NO_ITR_AVAILABLE = "No ITR- Income is assessed"

const val SMS_PLACE_HOLDER_VAR = "{#var#}"

const val MAX_LIMIT_TO_BULK_UPDATE = 1

const val LOAN_ACCOUNT_NUMBER = "loanAccountNumber"
const val FULLY_DISBURSED = "Fully Disbursed"
//const val IS_PRODUCTION = false

const val SF_PROPERTY_INSIGHT_ID = "sfPropertyInsightId"

const val FILE_NAMES = "fileNames"
const val SOURCE_HFO_SPRING = "HFO_SPRING"
const val SOURCE_WHATSAPP_BOT = "WHATSAPP_BOT"

enum class Actions(val value: String) {
    AUTHENTICATE_AGAIN("AUTHENTICATE_AGAIN"),
    RETRY("RETRY"),
    FIX_RETRY("FIX_RETRY"),
    CANCEL("CANCEL"),
    CONTACT_ADMIN("CONTACT_ADMIN"),
    DO_REGISTRATION("DO_REGISTRATION"),
    DO_VERIFICATION("DO_VERIFICATION"),
    GO_BACK("GO_BACK"),
    DO_LOGIN("DO_LOGIN"),
    DO_LOOKUP("DO_LOOKUP"),
    CONTINUE("CONTINUE");
}

enum class Errors(val value: String) {
    UNKNOWN("UNKNOWN"),
    FAILED("FAILED"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
    ACCESS_DENIED("ACCESS_DENIED"),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS"),
    DUPLICATE_RECORD("DUPLICATE_RECORD"),
    STRING_TOO_LONG("STRING_TOO_LONG"),
    JSON_PARSER_ERROR("JSON_PARSER_ERROR"),
    OPERATION_FAILED("OPERATION_FAILED"),
    INVALID_DATA("INVALID_DATA"),
    INVALID_REQUEST("INVALID_REQUEST"),
    INCONLUSIVE("INCONCLUSIVE"),
    SERVICE_NOT_FOUND("SERVICE_NOT_FOUND");
}

enum class EnLeadState(val value: String) {
    SUCCESS_CREATED_DB("SUCCESS-CREATED_DB"),
    SUCCESS_UPDATED_DB("SUCCESS-UPDATED_DB"),
    SUCCESS_CREATED_CRM("SUCCESS-CREATED_CRM"),
    ERROR_FAILED_CRM("ERROR-FAILED_CRM"),
    ERROR_STRING_TOO_LONG("ERROR-STRING_TOO_LONG"),
    ERROR_JSON_PARSER("ERROR-JSON_PARSER_ERROR"),
    ERROR_DUPLICATES_DETECTED("ERROR-DUPLICATES_DETECTED");

    companion object {
        operator fun get(value: String): EnLeadState? {
            for (stage: EnLeadState in values()) {
                if ((stage.value == value)) return stage
            }
            return null
        }
    }
}


enum class EnLeadStage(val value: String) {
    PROSPECT("Prospect"),
    CREDIT_REPORT_PULLED("Credit Report Pulled"),
    ASSIGNED_TO_BRANCH("Assigned to Branch"),
    VERIFICATION_SCHEDULED("Verification Scheduled"),
    VERIFICATION_DONE("Verification Done"),
    CONVERTED("Converted"),
    LOAN_SANCTIONED("Loan Sanctioned");

    companion object {
        operator fun get(value: String): EnLeadStage? {
            for (stage: EnLeadStage in EnLeadStage.values()) {
                if ((stage.value == value)) return stage
            }
            return null
        }
    }
}

enum class EnDocumentType(val value: String, val s3BucketPath: EnS3BucketPath?, val isDoubleSideDocument: Boolean) {
    AADHAAR("Aadhaar", EnS3BucketPath.AADHAAR_IMAGES, true),
    PAN("PAN", EnS3BucketPath.PAN_IMAGES, false),
    VOTER_ID("VoterId", EnS3BucketPath.VOTER_IMAGES, false),
    MOBILE("MOBILE", null, false),
    EMAIL("Email", null, false),
    CIBIL_REPORT("CibilReport", null, false),
    GSTIN("GSTIN", null, false),
    BANK_ACCOUNT("BankAccount", null, false),
    CHEQUE("cheque", null, false),
    PASSBOOK("passbook", null, false),
    PASSPORT("Passport", null, false),
    PROPERTY_INSIGHT_DIGITAL("PropertyInsightDigital", null, false),
    PROPERTY_INSIGHT_LEGAL("PropertyInsightLegal", null, false);

    companion object {
        operator fun get(value: String): EnDocumentType? {
            return values().find { it.value == value }
        }
    }
}

enum class LeadDestination(val value: String) {
    SALESFORCE("Salesforce"),
    LEAD_SQUARED("LeadSquared"),
    SF_N_LSQ("SF_N_LSQ");

    companion object {
        @Throws(Exception::class)
        operator fun get(value: String): LeadDestination {
            for (item: LeadDestination in values()) {
                if ((item.value == value)) return item
            }
            throw Exception("LeadDestination - Invalid value : $value")
        }
    }
}

enum class FileTypesExtentions(val ext: String, val contentType: String, val displayName: String) {
    PDF(".pdf", "application/pdf", "PDF"),
    HTML(".html", "text/html", "HTML"),
    MP3(".mp3", "audio/mpeg", "MP3"),
    CSV(".csv", "text/csv", "CSV"),
    JPEG(".jpeg", "image/jpeg", "Image"),
    JPG(".jpg", "image/jpeg", "Image"),
    PNG(".png", "image/png", "Image");


    companion object {

        fun getFileTypeFromContentType(contentType: String): FileTypesExtentions? {
            for (item: FileTypesExtentions in values()) {
                if ((item.contentType == contentType)) return item
            }
            return null
        }

        fun imageFormats() = listOf(
            ContentType.IMAGE_JPEG.mimeType,
            ContentType.IMAGE_PNG.mimeType
        )

        operator fun get(value: String): FileTypesExtentions? {
            for (ext: FileTypesExtentions in FileTypesExtentions.values()) {
                if ((ext.ext == value)) return ext
            }
            return null
        }
    }
}

enum class MyObject(val value: String) {
    DOCUMENT("Document"),
    KYC_DOCUMENT("KYCDocument"),
    BANK_ACCOUNT("BankAccount"),
    E_SIGN_DOCUMENT("ESignDocument"),
    LOAN("Loan"),
    BUREAU_REPORT("BureauReport"),
    BUREAU_REQUEST("BureauRequest"),
    APPLICANT("Applicant"),
    CONNECTOR("Connector"),
    E_STAMP_REQUEST("EStampRequest"),
    CSUB_PAYOUT("CSubPayout"),
    SITE_PHOTO("SitePhotograph"),
    LEAD("Lead"),
    USER("User"),
    ROLE("Role"),
    SALESGROUP("SalesGroup"),
    TEAM("Team"),
    ORGANIZATION("Organization"),
    PARTNER("Partner"),
    LEAD_SOURCE("LeadSource"),
    CAMPAIGN("Campaigns"),
    CROSS_NOTIFICATION("CrossNotification"),
    PROPERTY_INSIGHT("PropertyInsight"),
    CALL_LOG("CallLog"),
    TASK("Task"),
    LEAD_EXPORT("LeadExport"),
    SMS_TEMPLATE("SMSTemplate"),
    USER_REQUEST("cas_UserRequest"),
    SMS_LOG("SMSLog"),
    CONTACT("Contact"),
    NOTIFICATION_TOKEN("cas_NotificationToken"),
    NOTIFICATION("cas_Notification"),
    SF_LOAN_AMORT_REQUEST("SFLoanAmortRequest"),
    PROPERTY("Property"),
    MOBILE_NUMBER_LOOKUP("MobileNumberLookup");

    companion object {
        @Throws(Exception::class)
        operator fun get(value: String): MyObject {
            for (mo: MyObject in values()) {
                if ((mo.value == value)) return mo
            }
            throw Exception("No Object mapped for value: $value")
        }
    }
}

enum class MyService(val value: String) {
    SOFT_APPROVAL("softApproval"),
    E_SIGN_DOC("eSignDoc"),
    UPDATE_PAYOUT("CNAUpdatePayout"),
    BULK_IMPORT("CNABulkImport");
}

enum class AttachmentType(val value: String) {
    CIBIL_DOCUMENT("CibilDocument"),
    IDV_DOCUMENT("IdvDocument"),
    E_STAMP("eStamp"),
    AADHAAR("Aadhaar"),
    PAN("PAN"),
    CANCELLED_CHEQUE("CancelledCheque"),
    PASSBOOK("Passbook"),
    PROPERTY_INSIGHT("PropertyInsight"),
    AUDIO("Audio"),
    LMS_LEAD_EXPORT("LMSLeadExport"),
    PROPERTY_INSIGHT_DOCUMENT("PropertyInsightDocument"),
    VOTER_ID("VoterId"),
    PASSPORT("Passport"),
    DRIVING_LICENCE("DrivingLicence")
}

enum class EmailType(val value: String) {
    PERSONAL_EMAIL("Personal"), OFFICIAL_EMAIL("Official");
}

enum class OpportunityStage(val value: String) {
    DECLINED("0 - Declined"), LEAD_GENERATION("1 - Lead Generation"), INITIAL_SCREENING("2 - Initial Screening"), SOFT_APPROVAL_REQUEST(
        "3 - Soft Approval Request"
    ),
    SOFT_APPROVAL_CLARIFICATION("3A - Soft Approval Clarification"), SOFT_APPROVED("4 - Soft Approved"), SOFT_APPROVED_CANCELLED(
        "5 - Soft Approved Cancelled (SA-CNCLD)"
    ),
    FINAL_APPROVAL_REQUEST("6 - Final Approval Request"), FINAL_APPROVAL_CLARIFICATION("6A - Final Approval Clarification"), FINAL_APPROVED(
        "7 - Final Approved"
    ),
    PRICING_CLARIFICATION("7A - Pricing Clarification"), SYSTEM_APPROVED("8 - System Approved (Gradatim)"), SANCTION_LETTER_AND_DOCUMENTATON(
        "9 - Sanction Letter & Documentation"
    ),
    APPROVED_CANCELLED("10 - Approved Cancelled (AP-CNCLD)"), INSURANCE_OPPORTUNITY("11 - Insurance Opportunity");

    val isInProgress: Boolean
        get() = ((this == LEAD_GENERATION) || (this == INITIAL_SCREENING) || (this == SOFT_APPROVAL_REQUEST
                ) || (this == SOFT_APPROVAL_CLARIFICATION) || (this == SOFT_APPROVED) || (this == SOFT_APPROVED_CANCELLED
                ) || (this == FINAL_APPROVAL_REQUEST) || (this == FINAL_APPROVAL_CLARIFICATION) || (this == FINAL_APPROVED
                ) || (this == PRICING_CLARIFICATION))
    val isDeclined: Boolean
        get() {
            return (this == DECLINED || this == APPROVED_CANCELLED)
        }
    val isApproved: Boolean
        get() {
            return (this == SYSTEM_APPROVED || this == SANCTION_LETTER_AND_DOCUMENTATON)
        }

    fun generalStatus(): String {
        if (isInProgress) {
            return "In Progress"
        } else if (isDeclined) {
            return "Declined"
        } else if (isApproved) {
            return "Approved"
        } else {
            return NA
        }
    }

    companion object {
        operator fun get(value: String): OpportunityStage? {
            for (opp: OpportunityStage in values()) {
                if ((opp.value == value)) return opp
            }
            return null
        }
    }
}

enum class EnLoanApplicationStatus(val value: String) {
    IN_PROGRESS("In Progress"),
    DOCUMENT_COLLECTED("Documents Collected"),
    SANCTIONED("Sanctioned"),
    DISBURSED("Disbursed"),
    APPROVED("Approved"),
    DECLINED("Declined");

    companion object {
        fun getStatusByLead(status: String?): EnLoanApplicationStatus {

            //TODO: This fix is done because existing lead status was open and we look for Open
            val modStatus = status.let {
                if (it == "open") {
                    EnLeadStatus.OPEN.value
                } else
                    it
            }

            when (EnLeadStatus[modStatus]) {

                EnLeadStatus.QUALIFIED, EnLeadStatus.CONTACTED,
                EnLeadStatus.OPEN, EnLeadStatus.NONE -> return IN_PROGRESS

                EnLeadStatus.CLOSED -> return DECLINED

                else -> IN_PROGRESS
            }
            return IN_PROGRESS
        }

        fun getStatusByContact(status: String?): EnLoanApplicationStatus {
            when (ContactStatus[status]) {

                ContactStatus.WIP, ContactStatus.ASSIGNED_TO_BRANCH, ContactStatus.NONE -> return IN_PROGRESS

                ContactStatus.LOAN_SANCTIONED -> return SANCTIONED

                ContactStatus.CLOSED -> return DECLINED

                else -> IN_PROGRESS
            }
            return IN_PROGRESS
        }

        fun getStatusByOpportunity(status: String): EnLoanApplicationStatus {
            when (OpportunityStage[status]) {

                OpportunityStage.LEAD_GENERATION, OpportunityStage.INITIAL_SCREENING,
                OpportunityStage.SOFT_APPROVAL_REQUEST, OpportunityStage.SOFT_APPROVAL_CLARIFICATION,
                OpportunityStage.SOFT_APPROVED, OpportunityStage.SOFT_APPROVED_CANCELLED -> return IN_PROGRESS

                OpportunityStage.FINAL_APPROVAL_REQUEST,
                OpportunityStage.FINAL_APPROVAL_CLARIFICATION -> return DOCUMENT_COLLECTED

                OpportunityStage.FINAL_APPROVED, OpportunityStage.PRICING_CLARIFICATION,
                OpportunityStage.SYSTEM_APPROVED, OpportunityStage.SANCTION_LETTER_AND_DOCUMENTATON -> return SANCTIONED

                OpportunityStage.DECLINED, OpportunityStage.APPROVED_CANCELLED -> return DECLINED

                else -> {}
            }
            return IN_PROGRESS
        }
    }
}

enum class EnApplicantType(val value: String) {
    PRIMARY_APPLICANT("Primary"),
    CO_APPLICANT("Co-Applicant"),
    GUARANTOR("Guarantor");

    companion object {
        fun get(value: String): EnApplicantType? = values().find { it.value == value }
    }
}

enum class EnLeadScoreField(val value: String) {
    FIRSTNAME("firstName"),
    MIDDLE_NAME("middleName"),
    LASTNAME("lastName"),
    GENDER("gender"),
    DOB("dateOfBirth"),
    MOBILE("mobileNumber"),
    LOAN_AMOUNT("loanAmount"),
    LOAN_TYPE("loanType"),
    STREET("street"),
    CITY("city"),
    STATE("state"),
    COUNTRY("country"),
    POSTAL_CODE("postalCode"),
    TENURE("tenure"),
    PROPERTY_VALUE("propertyValue"),
    PROPERTY_TYPE("propertyType"),
    PROPERTY_IDENTIFIED("propertyIdentified"),
    ANY_EXISTING_LOAN("anyExistingLoan"),
    EXISTING_LOAN_EMI("existingLoanEMI"),
    AADHAAR("aadhaar"),
    PAN("pan"),
    DRIVER_LICENSE("driverLicense"),
    VOTER_ID("voterId"),
    PASSPORT("passport"),
    EMAIL("email"),
    HAS_CONSENTED("hasConsented"),
    MOBILE_VERIFIED("mobileVerified"),
    PHONE_VERIFIED("phoneVerified"),
    PERSONAL_EMAIL_VERIFIED("personalEmailVerified"),
    OFFICIAL_EMAIL_VERIFIED("officialEmailVerified"),
    PHONE_NUMBER("phoneNumber"),
    CIBIL_REPORT_URL("cibilReportUrl"),
    IDV_REPORT_URL("idvReportUrl"),
    EMPLOYMENT_TYPE("employmentType"),
    SALARIED_TYPE("salariedType"),
    ORGANIZATION("organization"),
    DESIGNATION("designation"),
    MONTHLY_INCOME("monthlyIncome"),
    MONTHLY_INCOME_IN_CASH("monthlyIncomeInCash"),
    ITR_AVAILABLE("itrAvailable"),
    YEAR_OF_WORK_EXPERIENCE("yearOfWorkExperience");


    companion object {
        fun get(value: String): EnLeadScoreField? = values().find { it.value == value }
    }
}

enum class EnProfessionType(val value: String) {
    SALARIED("Salaried"),
    PENSIONER("Pensioner"),
    HOUSE_WIFE("House wife"),
    SELF_EMPLOYED("Self Employed");

    companion object {
        fun getFrom(value: String): EnProfessionType? = values().find { it.value == value }
    }
}

enum class StateCode(val code: String, val stateName: String) {
    JAMMU_KASHMIR("01", "Jammu & Kashmir"), HIMACHAL_PRADESH("02", "Himachal Pradesh"), PUNJAB(
        "03",
        "Punjab"
    ),
    CHANDIGARH("04", "Chandigarh"), UTTARANCHAL("05", "Uttaranchal"), HARYANA("06", "Haryana"), DELHI(
        "07",
        "Delhi"
    ),
    RAJASTHAN("08", "Rajasthan"), UTTAR_PRADESH("09", "Uttar Pradesh"), BIHAR("10", "Bihar"), SIKKIM(
        "11",
        "Sikkim"
    ),
    ARUNACHAL_PRADESH("12", "Arunachal Pradesh"), NAGALAND("13", "Nagaland"), MANIPUR("14", "Manipur"), MIZORAM(
        "15",
        "Mizoram"
    ),
    TRIPURA("16", "Tripura"), MEGHALAYA("17", "Meghalaya"), ASSAM("18", "Assam"), WESTBENGAL(
        "19",
        "West Bengal"
    ),
    JHARKHAND("20", "Jharkhand"), Orissa("21", "Orissa"), Odisha("21", "Odisha"), Chhattisgarh(
        "22",
        "Chhattisgarh"
    ),
    Chattisgarh("22", "Chattisgarh"), Madhya_Pradesh("23", "Madhya Pradesh"), GUJARAT("24", "Gujarat"), DAMAN_DIU(
        "25",
        "Daman & Diu"
    ),
    DADRA_NAGAR_HAVELI("26", "Dadra & Nagar Haveli"), MAHARASHTRA("27", "Maharashtra"), ANDHRA_PRADESH(
        "28",
        "Andhra Pradesh"
    ),
    KARNATAKA("29", "Karnataka"), GOA("30", "Goa"), LAKSHADWEEP("31", "Lakshadweep"), KERALA(
        "32",
        "Kerala"
    ),
    TAMIL_NADU("33", "Tamil Nadu"), PONDICHERRY("34", "Pondicherry"), ANDAMAN_NICOBAR_ISLANDS(
        "35",
        "Andaman Nicobar Islands"
    ),
    TELANGANA("36", "Telangana"), APO_ADDRESS("99", "APO Address");

    companion object {
        operator fun get(name: String?): StateCode {
            for (sc: StateCode in values()) {
                if (sc.name.equals(name, ignoreCase = true)) return sc
            }
            return APO_ADDRESS
        }
    }
}

enum class ConnectorStage(val value: String) {
    DECLINED("Declined"), NEW("New"), SUBMITTED_FOR_APPROVAL("Submitted for Approval"), DOC_REVIEW_CLARIFICATION("Document Review Clarification"), DOC_REVIEWED(
        "Document Reviewed"
    ),
    APPROVED_FOR_SOURCING("Approved for Sourcing"), SOURCING_CLARIFICATION("Sourcing Clarification"), APPROVED_CANCELLED(
        "Approved Cancelled"
    );
}

enum class EnLeadStatus(val value: String, val displayName: String, val generalStatus: String) {
    QUALIFIED("Qualified", "Qualified", "In Progress"),
    CLOSED(
        "Closed Lead",
        "Closed",
        "Declined"
    ),
    CONTACTED("Contacted", "Contacted", "In Progress"),
    OPEN("Open", "Open", "In Progress"),
    NONE(
        "--None--",
        "--None--",
        "In Progress"
    );

    companion object {
        val filterStatus = arrayListOf(QUALIFIED.value, CLOSED.value)

        operator fun get(value: String?): EnLeadStatus? {
            return values().first { it.value == value }
        }
    }
}

enum class EnSubStatus(val value: String, val displayName: String, val status: String) {
    //    BLANK("--blank--","Blank","Open"),
    NC("NC", "NC", "Open"),
    RNR("RNR", "RNR", "Open"),
    SWO("SWO", "SWO", "Open"),

    //    CONTACTED_BLANK("--blank--","","Contacted"),
    CB("CB", "CB", "Contacted"),
    IH("IH", "IH", "Contacted"),
    IL("IL", "IL", "Contacted"),
    PROPERTY_UNIDENTIFIED("PROPERTY_UNIDENTIFIED", "Property Unidentified", "Contacted"),
    CUSTOMER_TO_VISIT("CUSTOMER_TO_VISIT", "Customer to visit branch within 10 days", "Contacted"),
    CIBIL_REQUEST("CIBIL_REQUEST", "Cibil request(post All Kyc received)", "Contacted"),
    KYC_PENDING("KYC_PENDING", "KYC pending", "Contacted"),

    //    CLOSED_BLANK("--blank--","","Closed Lead"),
    DSA_CONNECTOR("DSA_CONNECTOR", "DSA/Connector", "Closed Lead"),
    DUPLICATE("DUPLICATE", "Duplicate Lead", "Closed Lead"),
    HIGH_TICKET_SIZE("HIGH_TICKET_SIZE", "High Ticket Size", "Closed Lead"),
    LOW_TICKET_SIZE("LOW_TICKET_SIZE", "Low Ticket Size", "Closed Lead"),
    NI("NI", "NI", "Closed Lead"),
    NI_COMPETITOR("NI_COMPETITOR", "NI - Competitor", "Closed Lead"),
    NI_ROI("NI_ROI", "NI - ROI", "Closed Lead"),
    OSL("OSL", "Out of Service Location", "Closed Lead"),
    PL("PL", "Personal Loan", "Closed Lead"),
    CLOSED_RNR("CLOSED_RNR", "RNR", "Closed Lead"),
    SPAM("SPAM", "Spam Lead", "Closed Lead"),
    CLOSED_SWO("CLOSED_SWO", "SWO", "Closed Lead"),
    WN("WN", "WN", "Closed Lead"),
    NE_PROPERTY("NE_PROPERTY", "NE - Property", "Closed Lead"),
    NE_PROFILE("NE_PROFILE", "NE - Profile", "Closed Lead"),
    NE_CREDIT("NE_CREDIT", "NE - Credit Default", "Closed Lead");

    companion object {
        fun getAll(value: String?): Array<String> {
            val result: MutableList<String> = ArrayList()

            for (eachSub in values()) {
                if (eachSub.status == value) {
                    result.add(eachSub.displayName)
                }
            }
            return result.toTypedArray()

        }
    }

}

enum class EnLoanType(val value: String) {

    NONE("None"),
    HOME_LOAN("Home Loan"),
    PROPERTY_LOAN("Property Loan"),
    BUILDER_FINANCE("Builder Finance"),
    HOME_CONSTRUCTION_LOAN("Home Construction Loan"),
    MORTGAGE_LOAN("Mortgage Loan/Loan Against Property"),
    PERSONAL_LOAN("Personal Loan"),
    PLOT_LOAN("Plot Loan + HL (SeCo)"),
    OWN_PLOT("Own Plot + HL (SeCo)"),
    HOME_RESALE("Home Loan (Resale)"),
    LOAN_FOR_REPAIR("Loan for Repair / Renovation / Extension"),
    SHOP_LOAN("Shop Loan (Resale)"),
    LAP("Loan Against Property (LAP)"),
    LAP_COMMERCIAL("Loan Against Property against commercial property"),
    COMMERCIAL_LOAN("Commercial Property Loan"),
    HL_BT_RENOVATION("HL-BT + Renovation Loan"),
    HL_BT_SECO("HL-BT + SECO"),
    HOME_LOAN_BT("Home Loan (BT)"),
    HOME_LOAN_BT_POP_UP("Home Loan (BT) + Top Up"),
    LAP_BT("LAP-BT"),
    LAP_BT_RENOVATION("LAP-BT + Renovation Loan"),
    LAP_BT_SECO("LAP-BT + SECO"),
    LAP_BT_TOP_UP("LAP-BT + Top-Up"),
    PLOT_LOAN_BT_SECO("Plot Loan (BT) + SECO"),
    SHOP_LOAN_BT("Shop Loan - BT"),
    CONSTRUCTION_FINANCE_PRODUCT("Construction Finance Product"),
    SHOP_LOAN_BT_TOP_UP("Shop Loan BT + Top Up"),
    LTV_ENHANCEMENT("LTV Enhancement"),
    NON_CREDIT_INSURANCE("Non Credit Insurance"),
    TOP_UP_LOAN("Top Up Loan"),
    LAP_RENOVATION("LAP + Renovation"),
    LAP_SECO("LAP + SECO");

    companion object {
        operator fun get(value: String): EnLoanType? {
            return EnLoanType.values().first { it.value == value }
        }
    }

}

enum class PropertyType(val value: String) {

    LAP("Loan Against Property(LAP)"),
    PLOT_SECO("Plot + SeCo"),
    OWN_PLOT_SECO("Own Plot + SeCo"),
    PLOT_BT_SECO("Plot BT + SeCo"),
    BT("BT"),
    BT_TOPUP("BT + TopUp"),
    RESALE("Resale"),
    BUILDER_TIE_UP("Builder Tie-Up"),
    BUILDER_LAP("Builder LAP"),
    BUILDER_RESALE("Builder Resale"),
    BUILDER_BT_TOP_UP("Builder BT / BT + Top Up"),
    BUILDER_ONE_OFF("Builder One Off"),
    EXTENSION_RENOVATION("Extension / Renovation"),
    INDIVIDUAL_HOME_BUILDER("Individual Home Builder"),
    INDIVIDUAL_HOME_BUILDER_TIE_UP("Individual Home Builder Tie-Up"),
    PLOT("Plot");

    companion object {
        operator fun get(value: String): PropertyType? {
            return PropertyType.values().first { it.value == value }
        }
    }


}


enum class ContactStatus(val value: String) {
    WIP("WIP"), ASSIGNED_TO_BRANCH("Assigned to Branch"), CLOSED("Closed"), LOAN_SANCTIONED("Loan Sanctioned"), NONE("--None--");

    companion object {
        operator fun get(value: String?): ContactStatus? {
            return values().first { it.value == value }
        }
    }
}

enum class CredType(val value: String) {
    PRODUCTION("PRODUCTION"), UAT("UAT");
}

enum class SNDRecordType(val id: String, val type: String) {
    HOME_LOAN("01290000000ucIGAAY", "Home Loan"), TOPUP_LOAN("01290000000ucIHAAY", "Top Up / LTV Enhancement");

    companion object {
        operator fun get(id: String): SNDRecordType {
            return values().first { it.id == id }
        }
    }
}

enum class ConBankAccountType(val value: String, val sfKey: String) {
    SAVINGS("Savings", "Savings A/c"), CURRENT("Current", "Current A/c");

    companion object {
        operator fun get(value: String): ConBankAccountType? {
            return values().first { it.value == value }
        }

        fun getValueFromSF(sfKey: String): ConBankAccountType? {
            return values().first { it.sfKey == sfKey }
        }
    }
}

enum class Gender(val value: String) {
    MALE("Male"), FEMALE("Female"), OTHERS("Others");

    companion object {
        operator fun get(value: String): Gender? {
            return values().first { it.value == value }
        }
    }
}

enum class EnPartner(val value: String) {
    HOMEFIRST("homefirst")
}

enum class EnLMSOrg(val value: String) {
    HOMEFIRST("homefirst")
}

const val ROLE_SUPER_ADMIN = "SUPER_ADMIN"
const val ROLE_ADMIN = "ADMIN"
const val ROLE_MANAGEMENT = "MANAGEMENT"
const val ROLE_TEAM_HEAD = "TEAM_HEAD"
const val ROLE_TEAM_SUPERVISOR = "TEAM_SUPERVISOR"
const val ROLE_VRM = "VRM"
const val ROLE_VRO = "VRO"
const val ROLE_CALL_CENTER_SPOC = "CALL_CENTER_SPOC"
const val ROLE_CALL_CENTER_AGENT = "CALL_CENTER_AGENT"

const val AUTHORITY_SUPER_ADMIN = "hasAuthority('$ROLE_SUPER_ADMIN')"
const val AUTHORITY_ADMIN = "hasAuthority('$ROLE_ADMIN')"
const val AUTHORITY_MANAGEMENT = "hasAuthority('$ROLE_MANAGEMENT')"
const val AUTHORITY_TEAM_HEAD = "hasAuthority('$ROLE_TEAM_HEAD')"
const val AUTHORITY_TEAM_SUPERVISOR = "hasAuthority('$ROLE_TEAM_SUPERVISOR')"
const val AUTHORITY_VRM = "hasAuthority('$ROLE_VRM')"
const val AUTHORITY_VRO = "hasAuthority('$ROLE_VRO')"
const val AUTHORITY_CALL_CENTER_SPOC = "hasAuthority('$ROLE_CALL_CENTER_SPOC')"
const val AUTHORITY_CALL_CENTER_AGENT = "hasAuthority('$ROLE_CALL_CENTER_AGENT')"

const val AUTHORITY_LOWEST_LEVEL = "hasAuthority('$ROLE_VRO', '$ROLE_CALL_CENTER_AGENT')"

enum class EnRole(val value: String) {
    SUPER_ADMIN(ROLE_SUPER_ADMIN),
    ADMIN(ROLE_ADMIN),
    MANAGEMENT(ROLE_MANAGEMENT),
    TEAM_HEAD(ROLE_TEAM_HEAD),
    TEAM_SUPERVISOR(ROLE_TEAM_SUPERVISOR),
    VRM(ROLE_VRM),
    VRO(ROLE_VRO),
    CALL_CENTER_SPOC(ROLE_CALL_CENTER_SPOC),
    CALL_CENTER_AGENT(ROLE_CALL_CENTER_AGENT);

    companion object {

        val superRoles = arrayListOf(SUPER_ADMIN, ADMIN, MANAGEMENT)

        fun isTeamRequired(role: String): Boolean = !superRoles.contains(valueOf(role))
    }
}

enum class EnTeam(val value: String, val displayName: String) {
    DIGITAL("DIGITAL", "Digital"),
    STRATEGIC_ALLIANCE("STRATEGIC_ALLIANCE", "Strategic Alliance"),
    OFFLINE_MARKETING_SOUTH("OFFLINE_MARKETING_SOUTH", "Offline Marketing (South)"),
    OFFLINE_MARKETING_NORTH("OFFLINE_MARKETING_NORTH", "Offline Marketing (North)"),
    INSURANCE("INSURANCE", "Insurance")
}

enum class EnSalesGroup(
    val value: String,
    val displayName: String
) {
    OFM_NORTH("OFM_NORTH", "Offline Marketing North Team"),
    OFM_SOUTH("OFM_SOUTH", "Offline Marketing South Team"),
    SA_DEFAULT("SA_DEFAULT", "Strategic Alliances"),
    INS_DEFAULT("INS_DEFAULT", "Insurance"),
    DIGI_AKSHAY("DIGI_AKSHAY", "Akshay Team"),
    DIGI_ARIF("DIGI_ARIF", "Arif Team"),
    DIGI_PRANALI("DIGI_PRANALI", "Pranali Team"),
    DIGI_SHAILVI("DIGI_SHAILVI", "Shailvi Team"),
    DIGI_SURESH("DIGI_SURESH", "Suresh Team")
}

enum class EnUserType(val value: String) {
    INTERNAL("INTERNAL"),
    EXTERNAL("EXTERNAL"),
    CALL_CENTER("CALL_CENTER")
}

enum class CallStatus(val value: String) {
    CREATED("CREATED"),
    INITIATED("INITIATED"),
    SUCCESS("SUCCESS");
}

enum class SMSStatus(val value: String) {
    CREATED("created"),
    INITIATED("initiated"),
    SUCCESS("success");
}

enum class AdvancedFilterOperator(val value: String?) {
    EQUALS("equals"),
    NOT_EQ("not_equals"),
    GREATER_THAN("greater_than"),
    LESS_THAN("less_than"),
    LIKE("like"),
    IN("in"),
    STARTS_WITH("starts_with"),
    ENDS_WITH("ends_with"),
    CONTAINS("contains"),
    DOES_NOT_CONTAIN("does_not_contain"),
    BETWEEN("between"),
    NULL("null"),
    NOT_NULL("not_null");

    companion object {
        operator fun get(value: String): AdvancedFilterOperator {
            return AdvancedFilterOperator.values().first {
                it.value == value
            }
        }
    }
}

enum class AdvancedFilterType(val value: String) {
    LEAD("Lead"),
    ADDRESS("Address"),
    CONTACT("Contact"),
    USER("User");

    companion object {
        operator fun get(value: String): AdvancedFilterType {
            return AdvancedFilterType.values().first {
                it.value == value
            }
        }
    }
}


enum class EnBureauDocMapper(val value: String?) {
    CIBIL("CibilDocument"),
    IDV("IdvDocument");

    companion object {
        operator fun get(value: String): EnBureauDocMapper {
            return EnBureauDocMapper.values().first { it.value == value }
        }
    }
}

enum class EnApplicantRelations(val value: String?) {
    FATHER("Father"),
    MOTHER("Mother"),
    GRAND_FATHER("Grandfather"),
    GRAND_MOTHER("Grandmother"),
    BROTHER("Brother"),
    SISTER("Sister"),
    SPOUSE("Spouse"),
    SON("Son"),
    DAUGHTER("Daughter"),
    PARTNER("Partner"),
    OTHER("Other");

    companion object {
        val applicantRelations = arrayListOf(FATHER, MOTHER, BROTHER, SISTER, SPOUSE, SON, DAUGHTER, PARTNER)

        fun get(value: String): EnApplicantRelations? = EnApplicantRelations.values().find { it.value == value }

    }
}

enum class EnCallType(val value: String) {
    OUTBOUND("OUTBOUND"),
    INBOUND("INBOUND");
}

enum class EnCallPickUpStatus(val value: String) {
    NOANSWER("NOANSWER"),
    BUSY("BUSY"),
    FAILED("FAILED"),
    CANCEL("CANCEL"),
    CONGESTION("CONGESTION"),
    ANSWER("ANSWER")
}

enum class EnCallStatus(val value: String) {
    CREATED("CREATED"),
    INITIATED("INITIATED"),
    IVR_ANSWER("IVR-ANSWER"),
    CANCEL("CANCEL"),
    SUCCESS("SUCCESS");
}

enum class EnUserRequestStatus(val value: String) {
    CREATED("CREATED"),
    FAILED("FAILED"),
    PARTIALLY_COMPLETED("PARTIALLY_COMPLETED"),
    SUCCESS("SUCCESS");
}

enum class EnLeadUpdateType(val value: String) {
    PARTIALLY("PARTIALLY"),
    COMPLETELY("COMPLETELY")
}

enum class EnLeadExportType(val value: String) {
    LEAD_ID("LEAD_ID"),
    ADVANCED_FILTER("ADVANCED_FILTER")
}

enum class NotificationType(val value: String) {
    INBOUND_CALL("INBOUND_CALL"),
    LEAD_CONVERT("LEAD_CONVERT"),
    BUREAU("BUREAU"),
    MISSED_CALL("MISSED_CALL");
}

enum class EnSalaryType(val value: String, val displayName: String) {
    BANK("Bank", "Yes (Direct/Cheque)"),
    CASH("Cash", "No");

    companion object {
        operator fun get(value: String): EnSalaryType {
            return EnSalaryType.values().first { it.value == value }
        }
    }
}

enum class EnLeadSource(val value: String) {
    INBOUND_PHONE_CALL("Inbound Phone call"),
    TOLL_FREE_INCOMING("Toll Free Incoming");
}

enum class EnSMSType(val value: String) {
    SELF_ONBOARDING("SELF_ONBOARDING")
}

enum class EnGenderBureau(val value: String, val displayName: String) {
    MALE("Male", "M"),
    FEMALE("Female", "F"),
    OTHER("Other", "M");

    companion object {
        operator fun get(value: String): EnGenderBureau {
            return EnGenderBureau.values().first { it.value == value }
        }
    }
}

enum class EnConsentType(val value: String) {
    BUREAU("BUREAU"), CALL("CALL"), SMS("SMS"), WHATSAPP("WHATSAPP"), ALL("ALL");
}

enum class EnLoanAccountType(val value: String) {
    HOME_LOAN("HOME_LOAN"),
    TOPUP_LOAN("TOPUP_LOAN"),
    INSTANT_TOPUP_LOAN("INSTANT_TOPUP_LOAN")
}

enum class EnTransactionStatus {
    SUCCESS, FAILURE
}

enum class EnPropertyInsightStatus(val value: String) {
    INITIATED("INITIATED"),
    REQUESTED("REQUESTED"),
    PDF_REPORT_PULLED("PDF_REPORT_PULLED"),
    JSON_REPORT_PULLED("JSON_REPORT_PULLED"),
    COMPLETED("COMPLETED");

    companion object {
        operator fun get(value: String): EnPropertyInsightStatus? {
            for (pis in values()) {
                if (pis.value == value) return pis
            }
            return null
        }

        fun getPIPresentStatus(): ArrayList<String> {
            return ArrayList<String>().apply {
                this.add(REQUESTED.value)
                this.add(PDF_REPORT_PULLED.value)
                this.add(JSON_REPORT_PULLED.value)
                this.add(COMPLETED.value)
            }
        }

    }

}

enum class EnPICallbackCode(val value: String) {
    S001("S001"),
    S002("S002"),
    S003("S003"),
    P001("P001"),
    P002("P002"),
    E001("E001"),
    E002("E002"),
    E003("E003");

    companion object {
        operator fun get(value: String): EnPICallbackCode? {
            for (piCode in values()) {
                if (piCode.value == value) return piCode
            }
            return null
        }

        fun getSuccessCode(): ArrayList<String> {
            return ArrayList<String>().apply {
                this.add(S001.value)
                this.add(S002.value)
                this.add(S003.value)
            }
        }

        fun getNonProcessingCode(): ArrayList<String> {
            return ArrayList<String>().apply {
                this.add(E001.value)
                this.add(E002.value)
                this.add(E003.value)
            }
        }

    }

}

enum class EnPIReportType(val value: String) {
    DIGITAL_SEARCH_REPORT("Digital Property Search Report"),
    LEGAL_REPORT("Legal Due Diligence Report")
}

class ServiceRequestType(
    var requestType: String = NA,
    var categories: Array<String>
)

fun getServiceRequestTypeData(): ArrayList<ServiceRequestType> {

    val requestTypes: ArrayList<ServiceRequestType> = ArrayList()

    requestTypes.add(
        ServiceRequestType(
            "Select", arrayOf("Select")
        )
    )

    requestTypes.add(
        ServiceRequestType(
            "Disbursal Related Queries", arrayOf(
                "Disbursal Demand"
            )
        )
    )

    requestTypes.add(
        ServiceRequestType(
            "Documents Request", arrayOf(
                "Account Statement",
                "Interest Certificate",
                "Provisional Certificate",
                "Closure Letter",
                "Outstanding Letter",
                "List of Documents",
                "SOA+POS",
                "Receipt"
            )
        )
    )

    requestTypes.add(
        ServiceRequestType(
            "Top Up Loan", arrayOf(
                "Home Improvement / Furnishing",
                "Buy Out of Existing Debts",
                "Investment in Business",
                "Family Function",
                "Medical Purpose",
                "Land / Property Purchase",
                "Car Purchase",
                "Two Wheeler Purchase",
                "Education Purpose",
                "Others"


            )
        )
    )

    return requestTypes
}

enum class SalesforceObjectType(val value: String, val displayName: String) {
    COLLECTION("Collection__c", "Collection")
}

enum class CallSource(val value: String) {
    LMS("LMS"),
    SALESFORCE("SALESFORCE"),
    CMS("CMS"),
    RM_PRO("RM_PRO");


    companion object {
        fun get(value: String?): CallSource? {
            return values().find { it.value == value }
        }
    }
}

enum class EnOptInStatus(val value: String) {
    OPT_IN("OPT_IN")
}

enum class EnWhatsAppNumberStatus(val key: String){
    SUCCESS("SUCCESS"),
    UNKNOWN_SUBSCRIBER("UNKNOWN_SUBSCRIBER");

    companion object {
        fun get(key: String?): EnWhatsAppNumberStatus? {
            return EnWhatsAppNumberStatus.values().find { it.key == key }
        }
    }
}

enum class EnumStatus(val value: String) {
    CREATED("CREATED"),
    INITIATED("INITIATED"),
    SUCCESS("SUCCESS"),
    PENDING("PENDING");

    companion object {
        fun get(value: String?): EnumStatus? {
            return values().find { it.value == value }
        }
    }
}

enum class EnExternalServiceType(val value: String) {
    OCR("OCR"),
    VALIDATE("Validate");
}

enum class EnExternalServiceName(val value: String) {
    DIGITAP("DIGITAP")
}

enum class EnMobileValidationType(val value: String, val purpose: String) {
    INITIATE_REQUEST("INITIATE_REQUEST", "initiate_request"),
    SUBMIT_OTP("SUBMIT_OTP", "submit_otp"),
    GET_STATUS("GET_STATUS", "get_status");

    companion object {
        fun get(value: String): EnMobileValidationType? {
            return values().find { it.value == value }
        }
    }
}

enum class EnPaymentType(val nature:String, val subtypes: ArrayList<String>){
    EMI("EMI", arrayListOf("Equated Monthly Installment")),
    PARTIAL_PRE_PAYMENT("Partial Pre-Payment", arrayListOf("EMI Reduction", "Tenor Reduction"));

    companion object {
        fun get(nature: String?): EnPaymentType? {
            return EnPaymentType.values().find { it.nature == nature }
        }
    }

}

enum class EnLinkType(val value: String) {
    STANDARD("standard"),
    UPI("upi");

    companion object {
        operator fun get(value: String): EnLinkType? {
            for (item in values()) {
                if (item.value == value) return item
            }
            return null
        }
    }
}

