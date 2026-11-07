package io.github.kmplitert.core

class LiteRtException(
    val status: LiteRtStatus,
    val statusName: String,
    message: String
) : RuntimeException("$message ($statusName, status=$status)")

internal fun checkLiteRtStatus(status: LiteRtStatus, operation: String) {
    if (status != 0) {
        throw LiteRtException(
            status = status,
            statusName = liteRtStatusName(status),
            message = "$operation failed"
        )
    }
}

private fun liteRtStatusName(status: LiteRtStatus): String = when (status) {
    0 -> "kLiteRtStatusOk"
    1 -> "kLiteRtStatusErrorInvalidArgument"
    2 -> "kLiteRtStatusErrorMemoryAllocationFailure"
    3 -> "kLiteRtStatusErrorRuntimeFailure"
    4 -> "kLiteRtStatusErrorMissingInputTensor"
    5 -> "kLiteRtStatusErrorUnsupported"
    6 -> "kLiteRtStatusErrorNotFound"
    7 -> "kLiteRtStatusErrorTimeoutExpired"
    8 -> "kLiteRtStatusErrorWrongVersion"
    9 -> "kLiteRtStatusErrorUnknown"
    10 -> "kLiteRtStatusErrorAlreadyExists"
    11 -> "kLiteRtStatusErrorFailedPrecondition"
    100 -> "kLiteRtStatusCancelled"
    500 -> "kLiteRtStatusErrorFileIO"
    501 -> "kLiteRtStatusErrorInvalidFlatbuffer"
    502 -> "kLiteRtStatusErrorDynamicLoading"
    503 -> "kLiteRtStatusErrorSerialization"
    504 -> "kLiteRtStatusErrorCompilation"
    1000 -> "kLiteRtStatusErrorIndexOOB"
    1001 -> "kLiteRtStatusErrorInvalidIrType"
    1002 -> "kLiteRtStatusErrorInvalidGraphInvariant"
    1003 -> "kLiteRtStatusErrorGraphModification"
    1500 -> "kLiteRtStatusErrorInvalidToolConfig"
    2000 -> "kLiteRtStatusLegalizeNoMatch"
    2001 -> "kLiteRtStatusErrorInvalidLegalization"
    3000 -> "kLiteRtStatusPatternNoMatch"
    3001 -> "kLiteRtStatusInvalidTransformation"
    4000 -> "kLiteRtStatusErrorUnsupportedRuntimeVersion"
    4001 -> "kLiteRtStatusErrorUnsupportedCompilerVersion"
    4002 -> "kLiteRtStatusErrorIncompatibleByteCodeVersion"
    5000 -> "kLiteRtStatusErrorUnsupportedOpShapeInferer"
    5001 -> "kLiteRtStatusErrorShapeInferenceFailed"
    else -> "LiteRtStatus($status)"
}
