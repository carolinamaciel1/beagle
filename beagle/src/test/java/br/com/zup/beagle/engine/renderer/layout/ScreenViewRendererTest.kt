package br.com.zup.beagle.engine.renderer.layout

import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.com.zup.beagle.R
import br.com.zup.beagle.engine.renderer.RootView
import br.com.zup.beagle.engine.renderer.ViewRenderer
import br.com.zup.beagle.engine.renderer.ViewRendererFactory
import br.com.zup.beagle.extensions.once
import br.com.zup.beagle.setup.BeagleEnvironment
import br.com.zup.beagle.setup.DesignSystem
import br.com.zup.beagle.testutil.RandomData
import br.com.zup.beagle.view.BeagleFlexView
import br.com.zup.beagle.view.ViewFactory
import br.com.zup.beagle.widget.core.Flex
import br.com.zup.beagle.widget.core.FlexDirection
import br.com.zup.beagle.widget.core.JustifyContent
import br.com.zup.beagle.widget.core.Widget
import br.com.zup.beagle.widget.layout.NavigationBar
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import br.com.zup.beagle.widget.layout.ScreenWidget
private const val DEFAULT_COLOR = 0xFFFFFF

class ScreenViewRendererTest {

    @MockK
    private lateinit var screenWidget: ScreenWidget
    @MockK(relaxed = true)
    private lateinit var navigationBar: NavigationBar
    @MockK
    private lateinit var viewRendererFactory: ViewRendererFactory
    @MockK
    private lateinit var viewFactory: ViewFactory
    @MockK
    private lateinit var rootView: RootView
    @MockK
    private lateinit var context: AppCompatActivity
    @MockK
    private lateinit var beagleFlexView: BeagleFlexView
    @MockK
    private lateinit var widget: Widget
    @MockK
    private lateinit var viewRenderer: ViewRenderer<*>
    @MockK
    private lateinit var view: View
    @MockK(relaxed = true)
    private lateinit var actionBar: ActionBar

    private lateinit var screenViewRenderer: ScreenViewRenderer

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Color::class)
        mockkObject(BeagleEnvironment)

        every { viewFactory.makeBeagleFlexView(any()) } returns beagleFlexView
        every { viewFactory.makeBeagleFlexView(any(), any()) } returns beagleFlexView
        every { beagleFlexView.addView(any()) } just Runs
        every { beagleFlexView.addView(any(), any<Flex>()) } just Runs
        every { screenWidget.navigationBar } returns null
        every { screenWidget.header } returns null
        every { screenWidget.content } returns widget
        every { screenWidget.footer } returns null
        every { viewRendererFactory.make(any()) } returns viewRenderer
        every { viewRenderer.build(any()) } returns view
        every { Color.parseColor(any()) } returns DEFAULT_COLOR
        every { rootView.getContext() } returns context

        screenViewRenderer = ScreenViewRenderer(
            screenWidget,
            viewRendererFactory,
            viewFactory
        )
    }

    @After
    fun tearDown() {
        unmockkObject(BeagleEnvironment)
    }

    @Test
    fun build_should_create_a_screenWidget_with_flexDirection_COLUMN_and_justifyContent_SPACE_BETWEEN() {
        // Given
        val flexValues = mutableListOf<Flex>()
        every { viewFactory.makeBeagleFlexView(any(), capture(flexValues)) } returns beagleFlexView
        every { context.supportActionBar } returns null

        // When
        screenViewRenderer.build(rootView)


        // Then
        assertEquals(FlexDirection.COLUMN, flexValues[0].flexDirection)
        assertEquals(JustifyContent.SPACE_BETWEEN, flexValues[0].justifyContent)
    }

    @Test
    fun build_should_call_header_builder_and_add_to_screenWidget_view() {
        // Given
        every { screenWidget.header } returns widget
        every { context.supportActionBar } returns null

        // When
        screenViewRenderer.build(rootView)

        // Then
        verify(atLeast = once()) { viewRendererFactory.make(widget) }
        verify(atLeast = once()) { viewRenderer.build(rootView) }
        verify(atLeast = once()) { beagleFlexView.addView(view) }
    }

    @Test
    fun build_should_call_content_builder() {

        // Given
        val content = mockk<Widget>()
        val flex = slot<Flex>()
        every { screenWidget.content } returns content
        every { beagleFlexView.addView(view, capture(flex)) } just Runs
        every { context.supportActionBar } returns null

        // When
        screenViewRenderer.build(rootView)

        // Then
        verify(atLeast = once()) { viewRenderer.build(rootView) }
        verify(atLeast = once()) { beagleFlexView.addView(view, flex.captured) }
        assertEquals(1.0, flex.captured.grow)
    }

    @Test
    fun build_should_call_footer_builder_and_add_to_screenWidget_view() {
        // Given
        every { screenWidget.footer } returns widget
        every { context.supportActionBar } returns null

        // When
        screenViewRenderer.build(rootView)

        // Then
        verify(atLeast = once()) { viewRendererFactory.make(widget) }
        verify(atLeast = once()) { viewRenderer.build(rootView) }
        verify(atLeast = once()) { beagleFlexView.addView(view) }
    }

    @Test
    fun build_should_configure_toolbar_when_supportActionBar_is_not_null_and_toolbar_is_null() {
        // Given
        val title = RandomData.string()
        val showBackButton = true
        every { navigationBar.title } returns title
        every { navigationBar.showBackButton } returns showBackButton
        every { screenWidget.navigationBar } returns navigationBar
        every { context.supportActionBar } returns actionBar
        every { context.findViewById<Toolbar>(any()) } returns null

        // When
        screenViewRenderer.build(rootView)

        // Then
        verify(atLeast = once()) { actionBar.title = title }
        verify(atLeast = once()) { actionBar.setDisplayHomeAsUpEnabled(showBackButton) }
        verify(atLeast = once()) { actionBar.setDisplayShowHomeEnabled(showBackButton) }
        verify(atLeast = once()) { actionBar.show() }
    }

    @Test
    fun build_should_configure_toolbar_when_supportActionBar_is_not_null_and_toolbar_is_not_null() {
        // Given
        val toolbar = mockk<Toolbar>(relaxed = true)
        every { screenWidget.navigationBar } returns navigationBar
        every { context.supportActionBar } returns actionBar
        every { context.findViewById<Toolbar>(any()) } returns toolbar

        // When
        screenViewRenderer.build(rootView)

        // Then
        verify(atLeast = once()) { toolbar.visibility = View.VISIBLE }
    }

    @Test
    fun build_should_configure_toolbar_style_when_supportActionBar_is_not_null_and_toolbar_is_not_null() {
        // Given
        val toolbar = mockk<Toolbar>(relaxed = true)
        val designSystemMock = mockk<DesignSystem>()
        val typedArray = mockk<TypedArray>()
        val style = RandomData.string()
        val styleInt = RandomData.int()
        val navigationIcon = mockk<Drawable>()
        val titleTextAppearance = RandomData.int()
        val backgroundColorInt = RandomData.int()
        every { BeagleEnvironment.designSystem } returns designSystemMock
        every { designSystemMock.toolbarStyle(style) } returns styleInt
        every { navigationBar.style } returns style
        every { screenWidget.navigationBar } returns navigationBar
        every { context.supportActionBar } returns actionBar
        every { context.findViewById<Toolbar>(any()) } returns toolbar
        every {
            context.obtainStyledAttributes(styleInt, R.styleable.BeagleToolbarStyle)
        } returns typedArray
        every {
            typedArray.getDrawable(R.styleable.BeagleToolbarStyle_navigationIcon)
        } returns navigationIcon
        every {
            typedArray.getResourceId(R.styleable.BeagleToolbarStyle_titleTextAppearance, 0)
        } returns titleTextAppearance
        every {
            typedArray.getColor(R.styleable.BeagleToolbarStyle_android_background, 0)
        } returns backgroundColorInt
        every { typedArray.recycle() } just Runs

        // When
        screenViewRenderer.build(rootView)

        // Then
        verify(atLeast = once()) { toolbar.navigationIcon = navigationIcon }
        verify(atLeast = once()) { toolbar.setTitleTextAppearance(context, titleTextAppearance) }
        verify(atLeast = once()) { toolbar.setBackgroundColor(backgroundColorInt) }
        verify(atLeast = once()) { typedArray.recycle() }
    }
}
