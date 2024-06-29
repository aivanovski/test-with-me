package com.github.aivanovski.testwithme.android.presentation.core.cells

import com.github.aivanovski.testwithme.android.presentation.core.IntentProvider

interface CellViewModelFactory {

    fun createCellViewModels(
        models: List<BaseCellModel>,
        intentProvider: IntentProvider
    ): List<BaseCellViewModel> {
        return models.map { model -> createCellViewModel(model, intentProvider) }
    }

    fun createCellViewModel(
        model: BaseCellModel,
        intentProvider: IntentProvider
    ): BaseCellViewModel

    fun throwUnsupportedModelException(model: BaseCellModel): Nothing {
        throw IllegalArgumentException(
            "Unable to find ViewModel for model: ${model::class.qualifiedName}"
        )
    }
}