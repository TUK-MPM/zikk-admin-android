import com.google.gson.annotations.SerializedName

data class ReportResponse(
    val reportId: Long,
    val message: String,
    val reason: String,
    val createdAt: String,
    val repliedAt: String?
)
