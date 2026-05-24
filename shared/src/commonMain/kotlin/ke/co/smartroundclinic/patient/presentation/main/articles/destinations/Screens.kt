package ke.co.smartroundclinic.patient.presentation.main.articles.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ArticleList : NavKey

@Serializable
data class ArticleDetail(val articleId: String) : NavKey
