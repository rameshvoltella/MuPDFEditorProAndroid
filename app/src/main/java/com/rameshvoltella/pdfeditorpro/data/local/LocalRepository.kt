package com.rameshvoltella.pdfeditorpro.data.local

import com.artifex.mupdfdemo.MuPDFCore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository@Inject constructor(
    val localRepoData: LocalData
):LocalRepositorySource {
    override suspend fun getPageText(lastPageNumber: Int,totalPages: Int,muPDFCore: MuPDFCore): Flow<ArrayList<String>> {
        return flow {emit(localRepoData.getPageText(lastPageNumber,totalPages,muPDFCore))  }
    }
}