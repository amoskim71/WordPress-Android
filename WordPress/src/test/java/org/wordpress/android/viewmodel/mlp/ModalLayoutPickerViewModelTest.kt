package org.wordpress.android.viewmodel.mlp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.wordpress.android.MainCoroutineScopeRule
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import org.wordpress.android.util.NoDelayCoroutineDispatcher
import org.wordpress.android.viewmodel.mlp.ModalLayoutPickerViewModel.UiState.ContentUiState

@RunWith(MockitoJUnitRunner::class)
class ModalLayoutPickerViewModelTest {
    @Rule
    @JvmField val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @Rule
    @JvmField val coroutineScope = MainCoroutineScopeRule()

    private lateinit var viewModel: ModalLayoutPickerViewModel

    @Mock lateinit var dispatcher: Dispatcher
    @Mock lateinit var siteStore: SiteStore
    @Mock lateinit var appPrefsWrapper: AppPrefsWrapper
    @Mock lateinit var onCreateNewPageRequestedObserver: Observer<Unit>

    @Before
    fun setUp() {
        viewModel = ModalLayoutPickerViewModel(
                dispatcher,
                siteStore,
                appPrefsWrapper,
                NoDelayCoroutineDispatcher(),
                NoDelayCoroutineDispatcher()
        )
        viewModel.onCreateNewPageRequested.observeForever(
                onCreateNewPageRequestedObserver
        )
    }

    @ExperimentalCoroutinesApi
    private fun <T> mockFetchingSelectedSite(block: suspend CoroutineScope.() -> T) {
        coroutineScope.runBlockingTest {
            val siteId = 1
            val site = SiteModel()
            whenever(appPrefsWrapper.getSelectedSite()).thenReturn(siteId)
            whenever(siteStore.getSiteByLocalId(siteId)).thenReturn(site)
            whenever(siteStore.getSiteByLocalId(siteId)).thenReturn(site)
            block()
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when modal layout picker starts in landscape mode the title is visible`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.start(true)
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).isHeaderVisible).isEqualTo(true)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when modal layout picker starts in portrait mode the title is not visible`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.start(false)
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).isHeaderVisible).isEqualTo(false)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the user scroll beyond a threshold the title becomes visible`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onAppBarOffsetChanged(9, 10)
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).isHeaderVisible).isEqualTo(true)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the user scroll bellow a threshold the title remains hidden`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onAppBarOffsetChanged(11, 10)
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).isHeaderVisible).isEqualTo(false)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when modal layout picker starts the categories are loaded`() = mockFetchingSelectedSite {
        viewModel.init()
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).categories.size).isGreaterThan(0)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when modal layout picker starts the layouts are loaded`() = mockFetchingSelectedSite {
        viewModel.init()
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).layoutCategories.size).isGreaterThan(0)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `modal layout picker is shown when triggered`() = mockFetchingSelectedSite {
        viewModel.show()
        assertThat(viewModel.isModalLayoutPickerShowing.value!!.peekContent()).isEqualTo(true)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `modal layout picker is dismissed when the user hits the back button`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.dismiss()
        assertThat(viewModel.isModalLayoutPickerShowing.value!!.peekContent()).isEqualTo(false)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the create page is triggered the page creation flow starts`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.createPage()
        verify(onCreateNewPageRequestedObserver).onChanged(anyOrNull())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when modal layout picker starts no layout is selected`() = mockFetchingSelectedSite {
        viewModel.init()
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).selectedLayoutSlug).isNull()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the user taps on a layout the layout is selected if the thumbnail has loaded`() =
            mockFetchingSelectedSite {
                viewModel.init()
                viewModel.onThumbnailReady("about-1")
                viewModel.onLayoutTapped("about-1")
                assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).selectedLayoutSlug)
                        .isEqualTo("about-1")
            }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the user taps on a layout the layout is selected if the thumbnail has not loaded`() =
            mockFetchingSelectedSite {
                viewModel.init()
                viewModel.onLayoutTapped("about-1")
                assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).selectedLayoutSlug)
                        .isNotEqualTo("about-1")
            }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the user taps on a selected layout the layout is deselected`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onLayoutTapped("about-1")
        viewModel.onLayoutTapped("about-1")
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).selectedLayoutSlug).isNull()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the modal layout picker is dismissed the layout is deselected`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onLayoutTapped("about-1")
        viewModel.dismiss()
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).selectedLayoutSlug).isNull()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when modal layout picker starts no category is selected`() = mockFetchingSelectedSite {
        viewModel.init()
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).selectedCategoriesSlugs).isEmpty()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the user taps on a category the category is selected`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onCategoryTapped("about")
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).selectedCategoriesSlugs)
                .contains("about")
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the user taps on a selected category the category is deselected`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onCategoryTapped("about")
        viewModel.onCategoryTapped("about")
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).selectedCategoriesSlugs)
                .doesNotContain("about")
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when the modal layout picker is dismissed the category is deselected`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onCategoryTapped("about")
        viewModel.dismiss()
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).selectedCategoriesSlugs).isEmpty()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when no layout is selected the create blank page button is visible`() = mockFetchingSelectedSite {
        viewModel.init()
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).buttonsUiState.createBlankPageVisible)
                .isEqualTo(true)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when a layout is selected the create blank page button is not visible`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onThumbnailReady("about-1")
        viewModel.onLayoutTapped("about-1")
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).buttonsUiState.createBlankPageVisible)
                .isEqualTo(false)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when no layout is selected the create page button is not visible`() = mockFetchingSelectedSite {
        viewModel.init()
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).buttonsUiState.createPageVisible)
                .isEqualTo(false)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when a layout is selected the create page button is visible`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onThumbnailReady("about-1")
        viewModel.onLayoutTapped("about-1")
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).buttonsUiState.createPageVisible)
                .isEqualTo(true)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when no layout is selected the preview button is not visible`() = mockFetchingSelectedSite {
        viewModel.init()
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).buttonsUiState.previewVisible)
                .isEqualTo(false)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when a layout is selected the preview button is visible`() = mockFetchingSelectedSite {
        viewModel.init()
        viewModel.onThumbnailReady("about-1")
        viewModel.onLayoutTapped("about-1")
        assertThat(requireNotNull(viewModel.uiState.value as ContentUiState).buttonsUiState.previewVisible)
                .isEqualTo(true)
    }
}
