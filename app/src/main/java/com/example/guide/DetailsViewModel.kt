package com.example.guide

import androidx.lifecycle.ViewModel
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.BusinessObjectMetadata
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.mapkit.uri.UriObjectMetadata

data class DetailsDialogUiState(
    val title: String,
    val descriptionText: String,
    val location: Point?,
    val uri: String?,
    val typeSpecificState: TypeSpecificState,
)

sealed interface TypeSpecificState {
    data class Toponym(val address: String) : TypeSpecificState

    data class Business(
        val name: String,
        val workingHours: String?,
        val categories: String?,
        val phones: String?,
        val link: String?,
    ) : TypeSpecificState

    object Undefined : TypeSpecificState
}

class DetailsViewModel : ViewModel() {

    fun uiState(): DetailsDialogUiState? {
        val geoObject = SelectedObjectHolder.selectedObject ?: return null
        val uri = geoObject.metadataContainer.getItem(UriObjectMetadata::class.java).uris.firstOrNull()

        val geoObjetTypeUiState = geoObject.metadataContainer.getItem(ToponymObjectMetadata::class.java)?.let {
            TypeSpecificState.Toponym(address = it.address.formattedAddress)
        } ?: geoObject.metadataContainer.getItem(BusinessObjectMetadata::class.java)?.let {
            TypeSpecificState.Business(
                name = it.name,
                workingHours = it.workingHours?.text,
                categories = it.categories.map { it.name }.takeIfNotEmpty()?.toSet()
                    ?.joinToString(", "),
                phones = it.phones.map { it.formattedNumber }.takeIfNotEmpty()?.joinToString(", "),
                link = it.links.firstOrNull()?.link?.href,
            )
        } ?: TypeSpecificState.Undefined

        return DetailsDialogUiState(
            title = geoObject.name ?: "Нет названия",
            descriptionText = geoObject.descriptionText ?: "Нет описания",
            location = geoObject.geometry.firstOrNull()?.point,
            uri = uri?.value,
            typeSpecificState = geoObjetTypeUiState,
        )
    }
}
