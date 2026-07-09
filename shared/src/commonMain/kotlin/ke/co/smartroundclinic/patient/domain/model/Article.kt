package ke.co.smartroundclinic.patient.domain.model

data class Article(
    val id: String,
    val doctorId: String,
    val title: String,
    val content: String,
    val summary: String,
    val categoryId: String,
    val thumbnailUrl: String?,
    val state: ArticleState,
    val datePosted: String?,
    val createdAt: String,
    val updatedAt: String?,
    val authorName: String? = null,
)

enum class ArticleState { DRAFT, LIVE, SUSPENDED, DELETED }
