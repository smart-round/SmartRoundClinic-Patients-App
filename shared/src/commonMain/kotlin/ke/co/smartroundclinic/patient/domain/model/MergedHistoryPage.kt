package ke.co.smartroundclinic.patient.domain.model

/** One page of merged conversation history, plus the counterpart's read/delivered watermarks for computing ticks. */
data class MergedHistoryPage(
    val items: List<ConsultationMessage>,
    val nextCursor: String?,
    val counterpartLastReadAt: String?,
    val counterpartLastDeliveredAt: String?,
)
