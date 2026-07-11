package ke.co.smartroundclinic.patient.domain.model

/** One page of merged conversation history. */
data class MergedHistoryPage(
    val items: List<ConsultationMessage>,
    val nextCursor: String?,
)
